"use strict";
(function () {
  const grid = document.getElementById("grid");
  const stage = document.getElementById("stage");
  const hub = document.getElementById("hub");
  const backBtn = document.getElementById("backBtn");
  const subtitle = document.getElementById("subtitle");
  let cleanup = null;

  const hEl = (html) => { const d = document.createElement("div"); d.innerHTML = html; return d; };

  function showHub() {
    if (cleanup) { cleanup(); cleanup = null; }
    stage.classList.remove("active"); stage.innerHTML = "";
    hub.classList.add("active");
    backBtn.classList.remove("show");
    document.body.classList.remove("in-game");
    subtitle.textContent = "Play together — near or far";
    document.title = "Music_BG · Play";
  }
  function openGame(g) {
    if (cleanup) { cleanup(); cleanup = null; }
    hub.classList.remove("active");
    stage.innerHTML = ""; stage.classList.add("active");
    backBtn.classList.add("show");
    document.body.classList.add("in-game");
    subtitle.textContent = g.tag;
    cleanup = g.render(stage) || null;
    window.scrollTo(0, 0);
  }
  function openScoreboard() {
    if (cleanup) { cleanup(); cleanup = null; }
    hub.classList.remove("active"); stage.innerHTML = ""; stage.classList.add("active");
    backBtn.classList.add("show"); document.body.classList.add("in-game"); subtitle.textContent = "Your best scores";
    const wrap = document.createElement("div");
    wrap.appendChild(mk("div", "status", "🏆 Best Scores"));
    Object.keys(SCOREABLE).forEach((id) => {
      const row = mk("div", "station"); row.style.cursor = "default";
      row.innerHTML = `<span class="e">🎮</span><span><div class="t">${SCOREABLE[id]}</div><div class="d">Best: ${Store.best(id)}</div></span>`;
      wrap.appendChild(row);
    });
    wrap.appendChild(mk("div", "note", "🪙 " + Store.coins() + " coins · 🔥 " + Store.get("streak", 0) + "-day streak. Beat your best — scores save on this device."));
    stage.appendChild(wrap);
    window.scrollTo(0, 0);
  }
  backBtn.onclick = showHub;

  // ---------- helpers ----------
  const rnd = (n) => Math.floor(Math.random() * n);
  const mk = (tag, cls, txt) => { const e = document.createElement(tag); if (cls) e.className = cls; if (txt != null) e.textContent = txt; return e; };
  function swipe(node, cb) {
    let x0 = 0, y0 = 0;
    node.addEventListener("touchstart", (e) => { const t = e.touches[0]; x0 = t.clientX; y0 = t.clientY; }, { passive: true });
    node.addEventListener("touchend", (e) => {
      const t = e.changedTouches[0]; const dx = t.clientX - x0, dy = t.clientY - y0;
      if (Math.abs(dx) < 24 && Math.abs(dy) < 24) return;
      if (Math.abs(dx) > Math.abs(dy)) cb(dx > 0 ? "right" : "left"); else cb(dy > 0 ? "down" : "up");
    }, { passive: true });
  }

  // ---------- juice: sound + haptics + confetti ----------
  let actx = null;
  function beep(freq, dur, type) {
    try {
      actx = actx || new (window.AudioContext || window.webkitAudioContext)();
      if (actx.state === "suspended") actx.resume();
      const o = actx.createOscillator(), g = actx.createGain();
      o.type = type || "sine"; o.frequency.value = freq; o.connect(g); g.connect(actx.destination);
      g.gain.setValueAtTime(0.07, actx.currentTime);
      g.gain.exponentialRampToValueAtTime(0.0001, actx.currentTime + dur);
      o.start(); o.stop(actx.currentTime + dur);
    } catch (e) {}
  }
  const FX = {
    tap: () => beep(480, 0.05, "square"),
    pop: () => beep(720, 0.07, "sine"),
    hit: () => beep(300, 0.05, "triangle"),
    buzz: () => beep(110, 0.18, "sawtooth"),
    win: () => { [523, 659, 784, 1047].forEach((f, i) => setTimeout(() => beep(f, 0.16, "triangle"), i * 110)); FX.haptic(30); confetti(); },
    haptic: (ms) => { try { if (navigator.vibrate) navigator.vibrate(ms || 8); } catch (e) {} },
  };
  function confetti() {
    const c = document.createElement("canvas");
    c.style.cssText = "position:fixed;inset:0;pointer-events:none;z-index:60";
    c.width = window.innerWidth; c.height = window.innerHeight;
    document.body.appendChild(c);
    const x = c.getContext("2d"); if (!x) { c.remove(); return; }
    const cols = ["#e5484d", "#2fbf71", "#4c7df0", "#f2c14e", "#b06bff"];
    const P = Array.from({ length: 140 }, () => ({ x: Math.random() * c.width, y: -20 - Math.random() * c.height * 0.4, vx: (Math.random() - 0.5) * 4, vy: 2 + Math.random() * 5, s: 4 + Math.random() * 7, col: cols[rnd(cols.length)], a: Math.random() * 6, va: (Math.random() - 0.5) * 0.4 }));
    let t = 0;
    (function loop() {
      t++; x.clearRect(0, 0, c.width, c.height);
      P.forEach((p) => { p.x += p.vx; p.y += p.vy; p.vy += 0.05; p.a += p.va; x.save(); x.translate(p.x, p.y); x.rotate(p.a); x.fillStyle = p.col; x.fillRect(-p.s / 2, -p.s / 2, p.s, p.s * 0.5); x.restore(); });
      if (t < 170) requestAnimationFrame(loop); else c.remove();
    })();
  }

  // ---------- crisp canvas helper (logical WxH, DPR-scaled) ----------
  function makeCanvas(parent, W, H) {
    const cv = document.createElement("canvas");
    cv.style.cssText = "width:100%;display:block;border-radius:18px;touch-action:none;background:#0a0812;aspect-ratio:" + W + "/" + H;
    parent.appendChild(cv);
    const dpr = Math.min(window.devicePixelRatio || 1, 2);
    cv.width = W * dpr; cv.height = H * dpr;
    const ctx = cv.getContext("2d"); if (ctx) ctx.scale(dpr, dpr);
    const toLocal = (clientX, clientY) => { const r = cv.getBoundingClientRect(); return { x: (clientX - r.left) / r.width * W, y: (clientY - r.top) / r.height * H }; };
    return { cv, ctx, W, H, toLocal };
  }
  function roundRect(ctx, x, y, w, h, r) {
    ctx.beginPath(); ctx.moveTo(x + r, y);
    ctx.arcTo(x + w, y, x + w, y + h, r); ctx.arcTo(x + w, y + h, x, y + h, r);
    ctx.arcTo(x, y + h, x, y, r); ctx.arcTo(x, y, x + w, y, r); ctx.closePath();
  }
  const TAU = Math.PI * 2;

  // ---------- persistence: best scores, coins, daily streak ----------
  const Store = {
    get(k, d) { try { const v = localStorage.getItem("mbg_" + k); return v == null ? d : JSON.parse(v); } catch (e) { return d; } },
    set(k, v) { try { localStorage.setItem("mbg_" + k, JSON.stringify(v)); } catch (e) {} },
    best(id) { return Store.get("best_" + id, 0); },
    submitBest(id, score) { score = Math.round(score) || 0; if (score > Store.best(id)) { Store.set("best_" + id, score); return true; } return false; },
    coins() { return Store.get("coins", 0); },
    addCoins(n) { Store.set("coins", Store.coins() + n); },
  };
  const SCOREABLE = { flappy: "Flappy Tap", dashrun: "Dash Run", shooter: "Space Shooter", jumper: "Sky Hopper", bubble: "Bubble Pop", beat: "Beat Tap", g2048: "2048", snake: "Snake", whack: "Whack-a-Tap", stack: "Stack", meteor: "Meteor Dodge", aim: "Aim Trainer", simon: "Simon" };
  function dailyCheck() {
    const today = new Date().toISOString().slice(0, 10);
    const last = Store.get("lastDay", null);
    let streak = Store.get("streak", 0);
    let reward = 0;
    if (last !== today) {
      const y = new Date(Date.now() - 86400000).toISOString().slice(0, 10);
      streak = last === y ? streak + 1 : 1;
      reward = 10 + Math.min(streak, 10) * 5;
      Store.set("streak", streak); Store.set("lastDay", today); Store.addCoins(reward);
    }
    return { streak: Store.get("streak", 0), coins: Store.coins(), reward };
  }

  // ================= GAMES =================
  const games = [];

  // ---- Tic-Tac-Toe ----
  games.push({ id: "ttt", name: "Tic-Tac-Toe", tag: "Two players, one screen", emoji: "#️⃣", color: "#2fbf71",
    render(s) {
      let b = Array(9).fill(""), x = true, over = false;
      const W = [[0,1,2],[3,4,5],[6,7,8],[0,3,6],[1,4,7],[2,5,8],[0,4,8],[2,4,6]];
      const win = () => W.find(l => b[l[0]] && b[l[0]] === b[l[1]] && b[l[1]] === b[l[2]]);
      const st = mk("div", "status"); const board = mk("div", "board"); board.style.gridTemplateColumns = "repeat(3,1fr)";
      const cells = [];
      for (let i = 0; i < 9; i++) { const c = mk("button", "cell"); c.onclick = () => tap(i); board.appendChild(c); cells.push(c); }
      const btn = mk("button", "btn", "New game"); btn.onclick = reset;
      function draw() {
        cells.forEach((c, i) => { c.textContent = b[i]; c.style.color = b[i] === "X" ? "var(--coral)" : "var(--lav)"; });
        const w = win();
        st.textContent = w ? `${b[w[0]]} wins! 🎉` : (b.every(v => v) ? "Draw" : `${x ? "X" : "O"}'s turn`);
      }
      function tap(i) { if (over || b[i]) return; b[i] = x ? "X" : "O"; x = !x; if (win() || b.every(v => v)) over = true; draw(); }
      function reset() { b = Array(9).fill(""); x = true; over = false; draw(); }
      s.append(st, board, btn); draw();
    }});

  // ---- Connect Four ----
  games.push({ id: "c4", name: "Connect Four", tag: "Drop discs, line up four", emoji: "🔴", color: "#e5484d",
    render(s) {
      const R = 6, C = 7; let b = Array(R * C).fill(""), red = true, over = false;
      const win = () => { const d = [[0,1],[1,0],[1,1],[1,-1]];
        for (let r = 0; r < R; r++) for (let c = 0; c < C; c++) { const v = b[r*C+c]; if (!v) continue;
          for (const [dr, dc] of d) { let k = 1; while (k < 4) { const nr = r+dr*k, nc = c+dc*k; if (nr<0||nr>=R||nc<0||nc>=C||b[nr*C+nc]!==v) break; k++; } if (k===4) return v; } } return ""; };
      const st = mk("div", "status"); const board = mk("div", "board"); board.style.gridTemplateColumns = `repeat(${C},1fr)`;
      board.style.background = "var(--card)"; board.style.padding = "8px"; board.style.borderRadius = "16px";
      const cells = [];
      for (let i = 0; i < R * C; i++) { const c = mk("button", "cell"); c.style.borderRadius = "50%"; c.style.fontSize = "0"; c.onclick = () => drop(i % C); board.appendChild(c); cells.push(c); }
      const btn = mk("button", "btn", "New game"); btn.onclick = reset;
      function draw() { cells.forEach((c, i) => { c.style.background = b[i] === "R" ? "var(--coral)" : b[i] === "Y" ? "var(--amber)" : "var(--hi)"; });
        const w = win(); st.textContent = w ? `${w === "R" ? "Red" : "Yellow"} wins! 🎉` : (b.every(v => v) ? "Draw" : `${red ? "Red" : "Yellow"}'s turn`); }
      function drop(col) { if (over || win()) return; for (let r = R - 1; r >= 0; r--) { if (!b[r*C+col]) { b[r*C+col] = red ? "R" : "Y"; red = !red; if (win() || b.every(v => v)) over = true; draw(); return; } } }
      function reset() { b = Array(R*C).fill(""); red = true; over = false; draw(); }
      s.append(st, board, btn); draw();
    }});

  // ---- Memory Match ----
  games.push({ id: "mem", name: "Memory Match", tag: "Flip cards, find pairs", emoji: "🧠", color: "#b06bff",
    render(s) {
      const E = ["🎵","❤️","🎮","🌙","⭐","🎧","🔥","🍀"];
      let deck = shuffle([...E, ...E]).map((e, i) => ({ e, i, up: false, done: false }));
      let first = null, busy = false, moves = 0;
      const st = mk("div", "status"); const board = mk("div", "board"); board.style.gridTemplateColumns = "repeat(4,1fr)";
      const cells = deck.map((card, i) => { const c = mk("button", "cell"); c.onclick = () => tap(i); return c; });
      cells.forEach(c => board.appendChild(c));
      const btn = mk("button", "btn", "Shuffle & restart"); btn.onclick = () => { deck = shuffle([...E, ...E]).map((e,i)=>({e,i,up:false,done:false})); first=null;busy=false;moves=0;draw(); };
      function draw() { deck.forEach((c, i) => { const f = c.up || c.done; cells[i].textContent = f ? c.e : ""; cells[i].style.background = c.done ? "var(--card)" : "var(--hi)"; });
        const won = deck.every(c => c.done); st.textContent = won ? `Solved in ${moves} moves! 🎉` : `Moves: ${moves}`; }
      function tap(i) { if (busy) return; const c = deck[i]; if (c.up || c.done) return; c.up = true; draw();
        if (first === null) { first = i; } else { moves++; if (deck[first].e === c.e) { deck[first].done = c.done = true; first = null; draw(); }
          else { busy = true; const a = first, b = i; setTimeout(() => { if (deck[a]) deck[a].up = false; if (deck[b]) deck[b].up = false; first = null; busy = false; draw(); }, 700); } } }
      s.append(st, board, btn); draw();
    }});

  // ---- Reaction ----
  games.push({ id: "rx", name: "Reaction Duel", tag: "Tap the instant it turns green", emoji: "⚡", color: "#e5484d",
    render(s) {
      let phase = "idle", t0 = 0, best = 0, timer = null;
      const st = mk("div", "status"); const zone = mk("div", "tapzone"); zone.style.background = "#2a2440";
      const head = mk("div", "", "Tap to start"); const sub = mk("div", ""); sub.style.fontSize = "15px"; sub.style.fontWeight = "600"; sub.style.marginTop = "8px"; sub.textContent = "then wait for green";
      zone.append(head, sub);
      function set(bg, h, subT) { zone.style.background = bg; head.textContent = h; sub.textContent = subT || ""; }
      zone.onclick = () => {
        if (phase === "idle" || phase === "result" || phase === "soon") {
          phase = "wait"; set("#b23a48", "Wait…", "tap when it turns green");
          timer = setTimeout(() => { phase = "go"; t0 = performance.now(); set("#2fbf71", "TAP!", ""); }, 1200 + rnd(2500));
        } else if (phase === "wait") { clearTimeout(timer); phase = "soon"; set("#8a5a00", "Too soon!", "tap to retry"); }
        else if (phase === "go") { const ms = Math.round(performance.now() - t0); if (!best || ms < best) best = ms; phase = "result"; set("#2a2440", ms + " ms", "tap to retry"); st.textContent = "Best: " + best + " ms"; }
      };
      st.textContent = "Beat your best reaction time";
      s.append(st, zone, hEl('<div class="note">Couple mode: pass the phone and see who is quicker.</div>'));
      return () => clearTimeout(timer);
    }});

  // ---- Rock Paper Scissors ----
  games.push({ id: "rps", name: "Rock Paper Scissors", tag: "The age-old duel vs a bot", emoji: "✊", color: "#2fbf71",
    render(s) {
      const M = [["🪨","Rock"],["📄","Paper"],["✂️","Scissors"]]; let w=0,l=0,d=0;
      const beats = (a,b)=>(a===0&&b===2)||(a===1&&b===0)||(a===2&&b===1);
      const score = mk("div","status"); const faces = mk("div","center"); faces.style.margin="18px 0";
      const f = mk("div"); f.style.fontSize="56px"; f.textContent="✊  🤖  ✊"; const res = mk("div","status"); res.textContent="Choose your move"; faces.append(f);
      const row = mk("div","row");
      M.forEach((m,i)=>{ const b=mk("button","opt",m[0]+" "+m[1]); b.style.flex="1"; b.onclick=()=>play(i); row.appendChild(b); });
      row.className="seg";
      function play(i){ const bot=rnd(3); f.textContent=M[i][0]+"  vs  "+M[bot][0];
        if(i===bot){d++;res.textContent="Draw!";} else if(beats(i,bot)){w++;res.textContent="You win! 🎉";} else {l++;res.textContent="Bot wins";}
        score.textContent=`You ${w} · Bot ${l} · Draws ${d}`; }
      score.textContent="You 0 · Bot 0 · Draws 0";
      s.append(score,faces,res,row);
    }});

  // ---- This or That ----
  games.push({ id: "tot", name: "This or That", tag: "Two players — how alike are you?", emoji: "❤️", color: "#e5484d",
    render(s) {
      const P = [["Beach holiday","Mountain escape"],["Morning person","Night owl"],["Movie night in","Night out"],["Coffee","Tea"],
        ["Sweet","Savoury"],["Cats","Dogs"],["Plan it out","Go with the flow"],["City lights","Countryside"],["Cook in","Order out"],["Throwbacks","New releases"]];
      let i=0, p1=null, matches=0, done=0;
      const st=mk("div","status"); const card=mk("div","card"); const info=mk("div","note");
      function round(){ card.innerHTML=""; if(i>=P.length){ st.textContent=`You matched on ${matches}/${done} — ${pct()}% in sync`;
          const again=mk("button","btn","Play again"); again.onclick=()=>{i=0;p1=null;matches=0;done=0;st.textContent="";round();}; card.append(mk("div","center big",pct()+"%"),again); return; }
        st.textContent=`Round ${i+1}/${P.length} · ${pct()}% in sync`;
        const who=p1===null?"Player 1":"Player 2";
        card.append(mk("div","status",who+", pick one:"));
        const seg=mk("div","seg");
        P[i].forEach((opt,side)=>{ const b=mk("button","opt",opt); b.onclick=()=>pick(side); seg.appendChild(b); });
        card.append(seg);
      }
      function pick(side){ if(p1===null){ p1=side; round(); } else { done++; if(side===p1) matches++; p1=null; i++; round(); } }
      function pct(){ return done? Math.round(matches*100/done):0; }
      info.textContent="Pass-and-play: Player 1 picks, then hand over for Player 2.";
      s.append(st,card,info); round();
    }});

  // ---- 2048 ----
  games.push({ id: "g2048", name: "2048", tag: "Swipe to merge tiles", emoji: "🎯", color: "#2fbf71",
    render(s) {
      let g = Array(16).fill(0), score = 0;
      const st = mk("div","status"); const board = mk("div","board"); board.style.gridTemplateColumns="repeat(4,1fr)"; board.style.background="var(--card)"; board.style.padding="8px"; board.style.borderRadius="14px";
      const cells = Array.from({length:16},()=>{const c=mk("div","cell");c.style.fontSize="22px";return c;}); cells.forEach(c=>board.appendChild(c));
      const colors={0:"var(--hi)",2:"#3a3350",4:"#4a3f63",8:"#6d4b7a",16:"#8a4e77",32:"#b2506e",64:"#d65a64",128:"#e07a5f",256:"#e8965a",512:"#edb458",1024:"#f2d45c",2048:"#f7e463"};
      function spawn(){ const e=[]; g.forEach((v,i)=>{if(!v)e.push(i);}); if(!e.length)return; g[e[rnd(e.length)]]=Math.random()<.1?4:2; }
      function comp(line){ let a=line.filter(v=>v); const o=[]; for(let i=0;i<a.length;i++){ if(a[i]===a[i+1]){o.push(a[i]*2);score+=a[i]*2;i++;}else o.push(a[i]); } while(o.length<4)o.push(0); return o; }
      function move(dir){ const before=g.join(","); let rows=[];
        for(let r=0;r<4;r++){ let line=[0,1,2,3].map(c=>{ let idx; if(dir==="left")idx=r*4+c; else if(dir==="right")idx=r*4+(3-c); else if(dir==="up")idx=c*4+r; else idx=(3-c)*4+r; return g[idx]; });
          const n=comp(line); n.forEach((v,c)=>{ let idx; if(dir==="left")idx=r*4+c; else if(dir==="right")idx=r*4+(3-c); else if(dir==="up")idx=c*4+r; else idx=(3-c)*4+r; g[idx]=v; }); }
        if(g.join(",")!==before){ spawn(); draw(); } }
      function movesLeft(){ if(g.some(v=>!v))return true; for(let r=0;r<4;r++)for(let c=0;c<4;c++){ const v=g[r*4+c]; if(c<3&&v===g[r*4+c+1])return true; if(r<3&&v===g[(r+1)*4+c])return true; } return false; }
      function draw(){ cells.forEach((c,i)=>{ c.textContent=g[i]||""; c.style.background=colors[g[i]]||"#f7e463"; c.style.color=g[i]<=4?"var(--muted)":"#1a1030"; });
        if(!movesLeft())Store.submitBest("g2048",score); st.textContent = movesLeft()? "Score: "+score : "Game over · "+score; }
      swipe(board,move);
      const dpad=hEl(`<div class="dpad"><div></div><button class="dbtn" data-d="up">▲</button><div></div>
        <button class="dbtn" data-d="left">◀</button><div></div><button class="dbtn" data-d="right">▶</button>
        <div></div><button class="dbtn" data-d="down">▼</button><div></div></div>`).firstChild;
      dpad.querySelectorAll(".dbtn").forEach(b=>b.onclick=()=>move(b.dataset.d));
      const btn=mk("button","btn ghost","Restart"); btn.onclick=()=>{g=Array(16).fill(0);score=0;spawn();spawn();draw();};
      s.append(st,board,dpad,btn); spawn(); spawn(); draw();
    }});

  // ---- Snake ----
  games.push({ id: "snake", name: "Snake", tag: "Eat, grow, don't crash", emoji: "🐍", color: "#2fbf71",
    render(s) {
      const N=15; let snake=[Math.floor(N*N/2)], dir=1, food=Math.floor(N*N/2)+3, run=false, over=false, score=0, loop=null;
      const st=mk("div","status"); const board=mk("div","board"); board.style.gridTemplateColumns=`repeat(${N},1fr)`; board.style.background="var(--card)"; board.style.padding="4px"; board.style.borderRadius="12px";
      const cells=Array.from({length:N*N},()=>{const c=mk("div");c.style.aspectRatio="1";c.style.borderRadius="3px";return c;}); cells.forEach(c=>board.appendChild(c));
      function draw(){ cells.forEach((c,i)=>{ c.style.background = i===snake[0]?"var(--coral)": snake.includes(i)?"#8a3f56": i===food?"var(--teal)":"var(--hi)"; });
        if(over)Store.submitBest("snake",score); st.textContent = over? "Game over · "+score : "Score: "+score; }
      const DV={up:[-1,0],right:[0,1],down:[1,0],left:[0,-1]};
      let d="right";
      function tick(){ const [dr,dc]=DV[d]; const hr=Math.floor(snake[0]/N)+dr, hc=snake[0]%N+dc;
        if(hr<0||hr>=N||hc<0||hc>=N){over=true;run=false;clearInterval(loop);draw();return;}
        const nh=hr*N+hc; if(snake.includes(nh)){over=true;run=false;clearInterval(loop);draw();return;}
        const ate=nh===food; snake=[nh,...(ate?snake:snake.slice(0,-1))]; if(ate){score++; const free=[]; for(let i=0;i<N*N;i++)if(!snake.includes(i))free.push(i); food=free[rnd(free.length)]; } draw(); }
      function turn(nd){ const opp={up:"down",down:"up",left:"right",right:"left"}; if(nd===opp[d])return; d=nd; }
      function start(){ snake=[Math.floor(N*N/2)]; d="right"; food=Math.floor(N*N/2)+3; over=false; score=0; run=true; clearInterval(loop); loop=setInterval(tick,170); draw(); }
      swipe(board,turn);
      const dpad=hEl(`<div class="dpad"><div></div><button class="dbtn" data-d="up">▲</button><div></div>
        <button class="dbtn" data-d="left">◀</button><div></div><button class="dbtn" data-d="right">▶</button>
        <div></div><button class="dbtn" data-d="down">▼</button><div></div></div>`).firstChild;
      dpad.querySelectorAll(".dbtn").forEach(b=>b.onclick=()=>turn(b.dataset.d));
      const btn=mk("button","btn","Start / Restart"); btn.onclick=start;
      s.append(st,board,dpad,btn); draw();
      return ()=>clearInterval(loop);
    }});

  // ---- Gomoku (five in a row) ----
  games.push({ id:"gomoku", name:"Gomoku", tag:"Five in a row wins · 2P", emoji:"⚫", color:"#b06bff",
    render(s){ const N=12; let b=Array(N*N).fill(""), black=true, over=false;
      const win=()=>{const d=[[0,1],[1,0],[1,1],[1,-1]];for(let r=0;r<N;r++)for(let c=0;c<N;c++){const v=b[r*N+c];if(!v)continue;for(const[dr,dc]of d){let k=1;while(k<5){const nr=r+dr*k,nc=c+dc*k;if(nr<0||nr>=N||nc<0||nc>=N||b[nr*N+nc]!==v)break;k++;}if(k===5)return v;}}return"";};
      const st=mk("div","status");const board=mk("div","board");board.style.gridTemplateColumns=`repeat(${N},1fr)`;board.style.background="var(--card)";board.style.padding="4px";board.style.borderRadius="10px";
      const cells=[];for(let i=0;i<N*N;i++){const c=mk("button","cell");c.style.borderRadius="3px";c.style.fontSize="0";c.onclick=()=>tap(i);board.appendChild(c);cells.push(c);}
      function draw(){cells.forEach((c,i)=>{c.style.background=b[i]==="B"?"var(--text)":b[i]==="W"?"var(--coral)":"var(--hi)";});const w=win();st.textContent=w?`${w==="B"?"Black":"Coral"} wins! 🎉`:(b.every(v=>v)?"Draw":`${black?"Black":"Coral"}'s turn`);}
      function tap(i){if(over||b[i]||win())return;b[i]=black?"B":"W";black=!black;if(win())over=true;draw();}
      const btn=mk("button","btn","New game");btn.onclick=()=>{b=Array(N*N).fill("");black=true;over=false;draw();};
      s.append(st,board,btn);draw(); }});

  // ---- Would You Rather ----
  games.push({ id:"wyr", name:"Would You Rather", tag:"Impossible choices, together", emoji:"⚖️", color:"#2fbf71",
    render(s){ const D=[["Only music forever","Only movies forever"],["Read minds","Be invisible"],["Always 10 min late","Always 20 min early"],["Give up coffee","Give up dessert"],["Text only","Call only"],["Beach holiday","Mountain escape"],["Rewind button","Pause button"],["Know every lyric","Play any instrument"],["Teleport","Fly"],["Endless summer","Endless weekend"]];
      let order=shuffle(D.map((_,i)=>i)), i=0, ans=0, picked=false;
      const st=mk("div","status"); const wrap=mk("div");
      function round(){ wrap.innerHTML=""; picked=false; const d=D[order[i%order.length]]; st.textContent="Answered: "+ans;
        const a=mk("button","tapzone"); a.style.background="var(--lav)"; a.textContent=d[0];
        const orr=mk("div","center","or"); orr.style.color="var(--muted)"; orr.style.margin="10px 0";
        const bb=mk("button","tapzone"); bb.style.background="var(--coral)"; bb.textContent=d[1];
        a.onclick=bb.onclick=()=>{ if(picked)return; picked=true; ans++; i++; setTimeout(round,150); };
        wrap.append(a,orr,bb); }
      s.append(st,wrap,hEl('<div class="note">Great for two — debate it, then tap one to move on.</div>')); round(); }});

  // ---- Higher or Lower ----
  games.push({ id:"hilo", name:"Higher or Lower", tag:"Guess the secret number", emoji:"🔢", color:"#e5484d",
    render(s){ let target=1+rnd(100), lo=1, hi=100, guess=50, tries=0, done=false;
      const st=mk("div","status"); const num=mk("div","center big","50"); const hint=mk("div","status"); hint.textContent="I'm thinking of 1–100";
      const rowmm=mk("div","row"); const minus=mk("button","btn ghost","–"); const plus=mk("button","btn ghost","+");
      minus.onclick=()=>{guess=Math.max(lo,guess-1);num.textContent=guess;}; plus.onclick=()=>{guess=Math.min(hi,guess+1);num.textContent=guess;}; rowmm.append(minus,plus);
      const go=mk("button","btn","Guess");
      go.onclick=()=>{ if(done)return; tries++; if(guess===target){done=true;hint.textContent=`Got it in ${tries}! 🎉`;num.style.color="var(--teal)";go.textContent="Play again";go.onclick=()=>render2();}
        else if(guess<target){lo=guess+1;hint.textContent="Higher ⬆️";guess=Math.floor((lo+hi)/2);num.textContent=guess;} else {hi=guess-1;hint.textContent="Lower ⬇️";guess=Math.floor((lo+hi)/2);num.textContent=guess;} st.textContent=`Attempts: ${tries} · range ${lo}–${hi}`; };
      function render2(){ s.innerHTML=""; games.find(g=>g.id==="hilo").render(s); }
      st.textContent="Attempts: 0";
      s.append(st,num,hint,rowmm,go); }});

  // ---- Whack-a-Tap ----
  games.push({ id:"whack", name:"Whack-a-Tap", tag:"Hit the glowing tile, fast", emoji:"🎯", color:"#f2c14e",
    render(s){ let score=0, left=20, target=0, run=false, best=0, t1=null, t2=null;
      const st=mk("div","status"); const board=mk("div","board"); board.style.gridTemplateColumns="repeat(3,1fr)";
      const cells=Array.from({length:9},(_,i)=>{const c=mk("button","cell");c.onclick=()=>hit(i);board.appendChild(c);return c;});
      const btn=mk("button","btn","Start");
      function draw(){cells.forEach((c,i)=>{c.style.background=(run&&i===target)?"var(--coral)":"var(--hi)";c.textContent=(run&&i===target)?"🎯":"";});st.textContent=run?`Score ${score} · ${left}s`:(best?`Best: ${best}`:"Tap Start");}
      function moveT(){let n=rnd(9);if(n===target)n=(n+1)%9;target=n;draw();clearTimeout(t2);t2=setTimeout(moveT,850);}
      function hit(i){if(run&&i===target){score++;moveT();}}
      function start(){score=0;left=20;run=true;btn.style.display="none";moveT();clearInterval(t1);t1=setInterval(()=>{left--;draw();if(left<=0){run=false;clearInterval(t1);clearTimeout(t2);if(score>best)best=score;Store.submitBest("whack",score);btn.textContent="Play again";btn.style.display="";draw();}},1000);draw();}
      btn.onclick=start;
      s.append(st,board,btn); draw();
      return ()=>{clearInterval(t1);clearTimeout(t2);}; }});

  // ---- deck-cycler helper for conversation games ----
  function deckGame(cfg){ games.push({ id:cfg.id, name:cfg.name, tag:cfg.tag, emoji:cfg.emoji, color:cfg.color, cat:"Conversation",
    render(s){ let order=shuffle(cfg.cards.map((_,i)=>i)), i=0;
      const st=mk("div","status"); const card=mk("div","card"); card.style.minHeight="180px";
      function show(){ card.innerHTML=""; st.textContent=`Card ${i+1}`; card.append(mk("div","center","",), mk("div","center big",cfg.emoji)); const q=mk("div","status",(cfg.prefix||"")+cfg.cards[order[i%order.length]]); q.style.textAlign="center"; q.style.fontWeight="700"; card.append(q); }
      const b=mk("button","btn","Next"); b.onclick=()=>{ i++; if(i%order.length===0) order=shuffle(order); show(); };
      s.append(st,card,b,hEl(`<div class="note">${cfg.note}</div>`)); show();
    }}); }

  deckGame({ id:"nhie", name:"Never Have I Ever", tag:"Reveal & confess · 2P", emoji:"🙈", color:"#b06bff", prefix:"Never have I ever ",
    cards:["fallen asleep on a date","texted the wrong person something awkward","stalked an ex online","pretended to love a gift","had dessert for breakfast","sung loudly in the shower","re-gifted a present","had a crush on a friend’s partner","cried at a wedding","danced alone in my room","ghosted someone","faked being sick to skip plans"],
    note:"Take turns reading aloud. If you have — spill the story!" });

  deckGame({ id:"daily", name:"Daily Questions", tag:"One question a day, together", emoji:"💬", color:"#2fbf71", prefix:"",
    cards:["What made you smile today?","What’s a tiny thing I do that you love?","Where should we travel next?","What’s your favourite memory of us?","What are you grateful for right now?","If we had a free day tomorrow, what would we do?","What song matches your mood today?","What’s something new you want to try together?","What did you daydream about today?","When did you last feel really proud of me?","What’s your comfort meal this week?","What’s one thing on your mind right now?"],
    note:"Trade answers — no wrong replies, just talk." });

  deckGame({ id:"pillow", name:"Pillow Talk", tag:"Deeper questions for two", emoji:"🌙", color:"#e5484d", prefix:"",
    cards:["What first made you fall for me?","What does a perfect lazy day look like for us?","What’s a dream you haven’t told anyone?","How do you like to be comforted on a bad day?","What’s something you want us to do more of?","What are you most looking forward to with us?","What’s a fear you’ve been carrying?","What made you feel loved this week?","Where do you see us in five years?","What’s a small promise we can make tonight?"],
    note:"Lights low, phones down, just the two of you." });

  // ---- Truth or Dare ----
  games.push({ id:"tod", name:"Truth or Dare", tag:"Take turns · 2P", emoji:"🔥", color:"#e5484d", cat:"Conversation",
    render(s){ const T=["What was your first impression of me?","What’s something you’ve never told me?","What’s your favourite thing about us?","When did you feel closest to me?","What’s a secret talent of yours?","What’s on your bucket list?"];
      const D=["Send a voice note singing our song","Do your best impression of me","Text a compliment to the last person you messaged","Do a 15-second happy dance","Talk in an accent until your next turn","Give a dramatic toast to the other player"];
      let player=1, prompt=null, isT=true;
      const st=mk("div","status"); const card=mk("div","card"); card.style.minHeight="150px";
      const seg=mk("div","seg"); const t=mk("button","opt","Truth"); t.style.background="var(--lav)"; const d=mk("button","opt","Dare"); d.style.background="var(--coral)";
      const next=mk("button","btn","Done — next player"); next.style.display="none";
      t.onclick=()=>pick(true); d.onclick=()=>pick(false); next.onclick=()=>{ player=player===1?2:1; prompt=null; render2(); };
      function pick(truth){ isT=truth; const arr=truth?T:D; prompt=arr[rnd(arr.length)]; render2(); }
      function render2(){ st.textContent=`Player ${player}'s turn`; card.innerHTML="";
        if(prompt===null){ seg.style.display=""; next.style.display="none"; card.append(mk("div","center","",),mk("div","center","Pick Truth or Dare")); }
        else { seg.style.display="none"; next.style.display=""; card.append(mk("div","center",isT?"TRUTH":"DARE"),(()=>{const q=mk("div","status",prompt);q.style.textAlign="center";return q;})()); } }
      s.append(st,card,seg,next,hEl('<div class="note">Pass-and-play on one phone.</div>')); render2();
    }});

  // ---- Reversi ----
  games.push({ id:"reversi", name:"Reversi", tag:"Flank & flip · 2P", emoji:"⚪", color:"#2fbf71", cat:"Competitive",
    render(s){ const N=8; const DIRS=[[-1,-1],[-1,0],[-1,1],[0,-1],[0,1],[1,-1],[1,0],[1,1]];
      let b; function init(){ b=Array(N*N).fill(""); b[27]="W";b[28]="B";b[35]="B";b[36]="W"; turn="B"; }
      let turn="B";
      function flips(bd,idx,p){ if(bd[idx])return[]; const r=Math.floor(idx/N),c=idx%N,opp=p==="B"?"W":"B",out=[];
        for(const [dr,dc] of DIRS){ const line=[]; let nr=r+dr,nc=c+dc; while(nr>=0&&nr<N&&nc>=0&&nc<N&&bd[nr*N+nc]===opp){line.push(nr*N+nc);nr+=dr;nc+=dc;} if(line.length&&nr>=0&&nr<N&&nc>=0&&nc<N&&bd[nr*N+nc]===p)out.push(...line);} return out; }
      function moves(bd,p){ const m=[]; for(let i=0;i<N*N;i++) if(flips(bd,i,p).length) m.push(i); return m; }
      init();
      const st=mk("div","status"); const board=mk("div","board"); board.style.gridTemplateColumns=`repeat(${N},1fr)`; board.style.background="#1e6b4f"; board.style.padding="4px"; board.style.borderRadius="10px";
      const cells=[]; for(let i=0;i<N*N;i++){ const c=mk("button","cell"); c.style.background="#2a8f6b"; c.style.borderRadius="4px"; c.style.fontSize="0"; c.onclick=()=>play(i); board.appendChild(c); cells.push(c); }
      function draw(){ cells.forEach((c,i)=>{ c.innerHTML=""; if(b[i]){ const d=document.createElement("div"); d.style.width="72%"; d.style.height="72%"; d.style.margin="14% auto"; d.style.borderRadius="50%"; d.style.background=b[i]==="B"?"var(--text)":"var(--coral)"; c.appendChild(d);} });
        const cb=b.filter(v=>v==="B").length, cw=b.filter(v=>v==="W").length, mB=moves(b,"B").length, mW=moves(b,"W").length;
        st.textContent=(!mB&&!mW)?(cb>cw?`Dark wins ${cb}–${cw}!`:cw>cb?`Coral wins ${cw}–${cb}!`:"Tie!"):`● ${cb}  ○ ${cw} · ${turn==="B"?"Dark":"Coral"} to move`; }
      function play(i){ const f=flips(b,i,turn); if(!f.length)return; b[i]=turn; f.forEach(x=>b[x]=turn); const nx=turn==="B"?"W":"B"; turn=moves(b,nx).length?nx:turn; draw(); }
      const btn=mk("button","btn","New game"); btn.onclick=()=>{ init(); draw(); };
      s.append(st,board,btn); draw();
    }});

  // ---- Tap War ----
  games.push({ id:"tapwar", name:"Tap War", tag:"Fastest thumb wins · 2P", emoji:"👊", color:"#e5484d", cat:"Competitive",
    render(s){ const GOAL=30; let p1=0,p2=0,over=false;
      const st=mk("div","status"); st.textContent="First to "+GOAL+" taps wins";
      const z2=mk("button","tapzone"); z2.style.background="var(--teal)";
      const mid=mk("div","center","VS"); mid.style.color="var(--muted)"; mid.style.margin="8px 0";
      const z1=mk("button","tapzone"); z1.style.background="var(--coral)";
      function upd(){ z1.textContent="Player 1 · "+p1; z2.textContent="Player 2 · "+p2; if(!over){ if(p1>=GOAL){over=true;st.textContent="Player 1 wins! 🎉";} else if(p2>=GOAL){over=true;st.textContent="Player 2 wins! 🎉";} } }
      z1.onclick=()=>{ if(!over){p1++;upd();} }; z2.onclick=()=>{ if(!over){p2++;upd();} };
      const btn=mk("button","btn ghost","Reset"); btn.onclick=()=>{ p1=0;p2=0;over=false; st.textContent="First to "+GOAL+" taps wins"; upd(); };
      s.append(st,z2,mid,z1,btn); upd();
    }});

  // ---- Doodle Together ----
  games.push({ id:"doodle", name:"Doodle Together", tag:"One shared canvas", emoji:"🎨", color:"#b06bff", cat:"Co-op",
    render(s){ const cv=document.createElement("canvas"); cv.width=600; cv.height=600; cv.style.width="100%"; cv.style.background="#fff"; cv.style.borderRadius="14px"; cv.style.touchAction="none";
      const ctx=cv.getContext("2d"); if(ctx){ ctx.lineWidth=8; ctx.lineCap="round"; ctx.lineJoin="round"; }
      let cur="#e5484d", drawing=false, last=null;
      const P=(e)=>{ const r=cv.getBoundingClientRect(); const t=(e.touches&&e.touches[0])||e; return { x:(t.clientX-r.left)/r.width*cv.width, y:(t.clientY-r.top)/r.height*cv.height }; };
      cv.addEventListener("pointerdown",(e)=>{ if(!ctx)return; drawing=true; last=P(e); });
      cv.addEventListener("pointermove",(e)=>{ if(!ctx||!drawing)return; const p=P(e); ctx.strokeStyle=cur; ctx.beginPath(); ctx.moveTo(last.x,last.y); ctx.lineTo(p.x,p.y); ctx.stroke(); last=p; });
      window.addEventListener("pointerup",()=>{ drawing=false; });
      const pal=mk("div","row"); pal.style.marginTop="10px"; ["#e5484d","#2fbf71","#4c7df0","#f2c14e","#111111","#b06bff"].forEach(col=>{ const b=mk("button"); b.style.flex="1"; b.style.height="40px"; b.style.border="none"; b.style.borderRadius="10px"; b.style.background=col; b.onclick=()=>{cur=col;}; pal.appendChild(b); });
      const clr=mk("button","btn ghost","Clear"); clr.onclick=()=>{ if(ctx)ctx.clearRect(0,0,cv.width,cv.height); };
      s.append(mk("div","status","Draw together"), cv, pal, clr, hEl('<div class="note">Both of you can draw — huddle up or pass the phone.</div>'));
    }});

  // ---- Air Hockey (neon, 2 players, multitouch) ----
  games.push({ id:"airhockey", name:"Air Hockey", tag:"Neon 2-player duel", emoji:"🏒", color:"#4c7df0", cat:"Competitive",
    render(s){
      const st=mk("div","status"); st.textContent="0  —  0  · first to 7"; s.appendChild(st);
      const c=makeCanvas(s,340,560); if(!c.ctx) return; const {ctx,W,H,cv,toLocal}=c;
      const goalW=150, padR=26, puckR=15; let s1=0,s2=0,over=false,raf;
      const puck={x:W/2,y:H/2,vx:0,vy:0};
      const p1={x:W/2,y:H-70}, p2={x:W/2,y:70};
      const prev={p1:{x:p1.x,y:p1.y}, p2:{x:p2.x,y:p2.y}};
      const pointers={};
      function serve(dir){ puck.x=W/2; puck.y=H/2; puck.vx=(Math.random()-.5)*2; puck.vy=dir*3.4; }
      function assign(e){ const l=toLocal(e.clientX,e.clientY); pointers[e.pointerId]=l.y>H/2?"p1":"p2"; drag(e); }
      function drag(e){ const w=pointers[e.pointerId]; if(!w)return; const l=toLocal(e.clientX,e.clientY); const p=w==="p1"?p1:p2;
        p.x=Math.max(padR,Math.min(W-padR,l.x)); const lo=w==="p1"?H/2+padR:padR, hi=w==="p1"?H-padR:H/2-padR; p.y=Math.max(lo,Math.min(hi,l.y)); if(e.preventDefault)e.preventDefault(); }
      function drop(e){ delete pointers[e.pointerId]; }
      cv.addEventListener("pointerdown",assign); cv.addEventListener("pointermove",drag); cv.addEventListener("pointerup",drop); cv.addEventListener("pointercancel",drop);
      function collide(p,key){ const dx=puck.x-p.x, dy=puck.y-p.y, d=Math.hypot(dx,dy), min=puckR+padR;
        if(d<min && d>0){ const nx=dx/d, ny=dy/d; puck.x=p.x+nx*min; puck.y=p.y+ny*min; const pv={x:p.x-prev[key].x,y:p.y-prev[key].y}; const sp=Math.min(12,Math.hypot(puck.vx,puck.vy)+2.5); puck.vx=nx*sp+pv.x*0.7; puck.vy=ny*sp+pv.y*0.7; FX.hit(); } }
      function glow(x,y,r,col){ ctx.save(); ctx.shadowBlur=20; ctx.shadowColor=col; ctx.fillStyle=col; ctx.beginPath(); ctx.arc(x,y,r,0,TAU); ctx.fill(); ctx.restore(); }
      function step(){ if(over)return;
        puck.x+=puck.vx; puck.y+=puck.vy; puck.vx*=0.997; puck.vy*=0.997;
        if(puck.x<puckR){puck.x=puckR;puck.vx*=-1;} if(puck.x>W-puckR){puck.x=W-puckR;puck.vx*=-1;}
        const g0=(W-goalW)/2, g1=(W+goalW)/2;
        if(puck.y<puckR){ if(puck.x>g0&&puck.x<g1){s1++;score();} else {puck.y=puckR;puck.vy*=-1;} }
        if(puck.y>H-puckR){ if(puck.x>g0&&puck.x<g1){s2++;score();} else {puck.y=H-puckR;puck.vy*=-1;} }
        collide(p1,"p1"); collide(p2,"p2"); prev.p1={x:p1.x,y:p1.y}; prev.p2={x:p2.x,y:p2.y};
        ctx.clearRect(0,0,W,H);
        ctx.strokeStyle="rgba(255,255,255,.12)"; ctx.lineWidth=2; ctx.beginPath();ctx.moveTo(0,H/2);ctx.lineTo(W,H/2);ctx.stroke(); ctx.beginPath();ctx.arc(W/2,H/2,52,0,TAU);ctx.stroke();
        ctx.lineWidth=6; ctx.strokeStyle="#4c7df0"; ctx.beginPath();ctx.moveTo((W-goalW)/2,3);ctx.lineTo((W+goalW)/2,3);ctx.stroke();
        ctx.strokeStyle="#e5484d"; ctx.beginPath();ctx.moveTo((W-goalW)/2,H-3);ctx.lineTo((W+goalW)/2,H-3);ctx.stroke();
        glow(p1.x,p1.y,padR,"#e5484d"); glow(p2.x,p2.y,padR,"#4c7df0"); glow(puck.x,puck.y,puckR,"#ffffff");
        if(s1>=7||s2>=7){ over=true; st.textContent=(s1>=7?"Player 1 (bottom) wins!":"Player 2 (top) wins!")+" 🎉"; FX.win(); return; }
        raf=requestAnimationFrame(step);
      }
      function score(){ st.textContent=s1+"  —  "+s2+"  · first to 7"; FX.buzz(); FX.haptic(20); serve(s1>s2?1:-1); }
      serve(1); step();
      s.append(hEl('<div class="note">Player 1 drags the red paddle (bottom), Player 2 the blue (top). It\'s multi-touch — play at the same time.</div>'));
      return ()=>{ over=true; cancelAnimationFrame(raf); };
    }});

  // ---- Beat Tap (rhythm) ----
  games.push({ id:"beat", name:"Beat Tap", tag:"Tap to the rhythm", emoji:"🎶", color:"#2fbf71", cat:"Arcade",
    render(s){
      const st=mk("div","status"); s.appendChild(st);
      const c=makeCanvas(s,340,520); if(!c.ctx) return; const {ctx,W,H,cv,toLocal}=c;
      const cols=4, colW=W/cols, hitY=H-70, colColors=["#e5484d","#2fbf71","#4c7df0","#f2c14e"];
      let notes=[],score=0,combo=0,best=0,life=5,spawnT=0,over=false,raf;
      function hit(col){ let bi=-1,bd=1e9; notes.forEach((n,i)=>{ if(n.c===col&&!n.done){ const d=Math.abs(n.y-hitY); if(d<bd){bd=d;bi=i;} } });
        if(bi>=0&&bd<44){ notes[bi].done=true; score+=10+combo; combo++; if(combo>best)best=combo; FX.pop(); FX.haptic(6); } else { combo=0; FX.buzz(); } }
      cv.addEventListener("pointerdown",e=>{ if(over){restart();return;} const l=toLocal(e.clientX,e.clientY); hit(Math.max(0,Math.min(cols-1,Math.floor(l.x/colW)))); });
      function step(){ if(!over){ spawnT--; if(spawnT<=0){ notes.push({c:rnd(cols),y:-30,done:false}); spawnT=38+rnd(28); } }
        ctx.clearRect(0,0,W,H);
        for(let i=0;i<cols;i++){ ctx.fillStyle="rgba(255,255,255,"+(i%2?0.03:0.06)+")"; ctx.fillRect(i*colW,0,colW,H); }
        ctx.strokeStyle="#fff"; ctx.lineWidth=3; ctx.beginPath();ctx.moveTo(0,hitY);ctx.lineTo(W,hitY);ctx.stroke();
        notes.forEach(n=>{ if(n.done)return; n.y+=4.2; ctx.save();ctx.shadowBlur=14;ctx.shadowColor=colColors[n.c];ctx.fillStyle=colColors[n.c]; roundRect(ctx,n.c*colW+8,n.y,colW-16,26,8);ctx.fill();ctx.restore();
          if(n.y>hitY+34){ n.done=true; combo=0; life--; FX.buzz(); if(life<=0){over=true;Store.submitBest("beat",score);} } });
        for(let i=notes.length-1;i>=0;i--) if(notes[i].done && notes[i].y>H+40) notes.splice(i,1);
        st.textContent = over? ("Score "+score+" · best combo "+best+" · tap to retry") : ("Score "+score+" · Combo "+combo+" · ❤"+Math.max(0,life));
        if(over){ ctx.fillStyle="rgba(0,0,0,.6)";ctx.fillRect(0,0,W,H); ctx.fillStyle="#fff";ctx.textAlign="center";ctx.font="bold 28px sans-serif";ctx.fillText(""+score,W/2,H/2); ctx.font="16px sans-serif";ctx.fillStyle="#a99fc0";ctx.fillText("tap to retry",W/2,H/2+28); }
        raf=requestAnimationFrame(step);
      }
      function restart(){ notes=[];score=0;combo=0;life=5;spawnT=0;over=false; }
      step();
      s.append(hEl('<div class="note">Tap a column the moment its tile hits the white line. Keep the combo alive!</div>'));
      return ()=>cancelAnimationFrame(raf);
    }});

  // ---- Bubble Pop ----
  games.push({ id:"bubble", name:"Bubble Pop", tag:"Pop fast · 30 seconds", emoji:"🫧", color:"#b06bff", cat:"Arcade",
    render(s){
      const st=mk("div","status"); s.appendChild(st);
      const c=makeCanvas(s,340,520); if(!c.ctx) return; const {ctx,W,H,cv,toLocal}=c;
      const cols=["#e5484d","#2fbf71","#4c7df0","#f2c14e","#b06bff"];
      let bubbles=[],parts=[],score=0,time=30,over=false,spawnT=0,last=performance.now(),acc=0,raf;
      cv.addEventListener("pointerdown",e=>{ if(over){restart();return;} const l=toLocal(e.clientX,e.clientY);
        for(let i=bubbles.length-1;i>=0;i--){ const b=bubbles[i]; if(Math.hypot(b.x-l.x,b.y-l.y)<b.r){ burst(b); bubbles.splice(i,1); score++; FX.pop(); FX.haptic(6); break; } } });
      function burst(b){ for(let k=0;k<10;k++) parts.push({x:b.x,y:b.y,vx:(Math.random()-.5)*7,vy:(Math.random()-.5)*7,life:22,col:b.col}); }
      function restart(){ bubbles=[];parts=[];score=0;time=30;over=false;spawnT=0;last=performance.now();acc=0; }
      function step(now){ now=now||performance.now(); const dt=now-last; last=now; if(!over){ acc+=dt; if(acc>=1000){acc-=1000;time--; if(time<=0){over=true;FX.win();Store.submitBest("bubble",score);}} }
        if(!over){ spawnT--; if(spawnT<=0){ const r=18+rnd(16); bubbles.push({x:r+rnd(W-2*r),y:H+r,r,vy:1+Math.random()*1.7,col:cols[rnd(cols.length)]}); spawnT=16+rnd(18); } }
        ctx.clearRect(0,0,W,H);
        bubbles.forEach(b=>{ b.y-=b.vy; ctx.save();ctx.globalAlpha=.9;ctx.shadowBlur=14;ctx.shadowColor=b.col;ctx.fillStyle=b.col;ctx.beginPath();ctx.arc(b.x,b.y,b.r,0,TAU);ctx.fill();ctx.restore(); });
        bubbles=bubbles.filter(b=>b.y>-b.r-10);
        parts.forEach(p=>{ p.x+=p.vx; p.y+=p.vy; p.life--; ctx.globalAlpha=Math.max(0,p.life/22); ctx.fillStyle=p.col; ctx.fillRect(p.x,p.y,4,4); }); ctx.globalAlpha=1; parts=parts.filter(p=>p.life>0);
        st.textContent = over? ("Time! Score "+score+" · tap to retry") : ("Score "+score+" · ⏱ "+time+"s");
        if(over){ ctx.fillStyle="rgba(0,0,0,.55)";ctx.fillRect(0,0,W,H); ctx.fillStyle="#fff";ctx.textAlign="center";ctx.font="bold 30px sans-serif";ctx.fillText("Score "+score,W/2,H/2); }
        raf=requestAnimationFrame(step);
      }
      step();
      s.append(hEl('<div class="note">Tap bubbles before they float away. How many can you pop in 30s?</div>'));
      return ()=>cancelAnimationFrame(raf);
    }});

  // ---- Brick Breaker ----
  games.push({ id:"brick", name:"Brick Breaker", tag:"Clear every brick", emoji:"🧱", color:"#f2c14e", cat:"Arcade",
    render(s){
      const st=mk("div","status"); s.appendChild(st);
      const c=makeCanvas(s,340,520); if(!c.ctx) return; const {ctx,W,H,cv,toLocal}=c;
      const padW=74,padH=12,br=7; let pad,ball,bricks,lives,over,won,launched,raf,celebrated;
      function level(){ pad={x:W/2-padW/2,y:H-30}; ball={x:W/2,y:H-46,vx:0,vy:0}; launched=false; over=false; won=false; celebrated=false; lives=3;
        bricks=[]; const rows=5,cn=6,bw=(W-20)/cn,bh=22,bc=["#e5484d","#f2c14e","#2fbf71","#4c7df0","#b06bff"];
        for(let r=0;r<rows;r++)for(let cc=0;cc<cn;cc++)bricks.push({x:10+cc*bw,y:52+r*(bh+6),w:bw-6,h:bh,col:bc[r%bc.length],alive:true}); }
      level();
      cv.addEventListener("pointerdown",e=>{ if(over||won){level();return;} launched=true; aim(e); });
      cv.addEventListener("pointermove",aim);
      function aim(e){ const l=toLocal(e.clientX,e.clientY); pad.x=Math.max(0,Math.min(W-padW,l.x-padW/2)); if(!launched)ball.x=pad.x+padW/2; if(e.preventDefault)e.preventDefault(); }
      function step(){ ctx.clearRect(0,0,W,H);
        if(launched && ball.vx===0 && ball.vy===0){ ball.vx=2.6; ball.vy=-3.6; }
        if(launched && !over && !won){ ball.x+=ball.vx; ball.y+=ball.vy;
          if(ball.x<br){ball.x=br;ball.vx*=-1;} if(ball.x>W-br){ball.x=W-br;ball.vx*=-1;} if(ball.y<br){ball.y=br;ball.vy*=-1;}
          if(ball.y>pad.y-br && ball.y<pad.y+padH && ball.x>pad.x && ball.x<pad.x+padW && ball.vy>0){ ball.vy*=-1; ball.y=pad.y-br; ball.vx=((ball.x-(pad.x+padW/2))/(padW/2))*4.2; FX.hit(); }
          if(ball.y>H+20){ lives--; FX.buzz(); if(lives<=0)over=true; else { ball.x=pad.x+padW/2; ball.y=pad.y-16; ball.vx=0; ball.vy=0; launched=false; } }
          bricks.forEach(b=>{ if(b.alive && ball.x>b.x && ball.x<b.x+b.w && ball.y>b.y && ball.y<b.y+b.h){ b.alive=false; ball.vy*=-1; FX.pop(); } });
          if(bricks.every(b=>!b.alive)){ won=true; if(!celebrated){celebrated=true;FX.win();} }
        }
        bricks.forEach(b=>{ if(!b.alive)return; ctx.save();ctx.shadowBlur=8;ctx.shadowColor=b.col;ctx.fillStyle=b.col; roundRect(ctx,b.x,b.y,b.w,b.h,5);ctx.fill();ctx.restore(); });
        ctx.fillStyle="#f3eefb"; roundRect(ctx,pad.x,pad.y,padW,padH,6); ctx.fill();
        ctx.save();ctx.shadowBlur=12;ctx.shadowColor="#fff";ctx.fillStyle="#fff";ctx.beginPath();ctx.arc(ball.x,ball.y,br,0,TAU);ctx.fill();ctx.restore();
        st.textContent = won? "Cleared! 🎉 tap to replay" : over? "Game over · tap to replay" : ("Lives: "+lives+(launched?"":" · tap to launch"));
        if(over||won){ ctx.fillStyle="rgba(0,0,0,.5)";ctx.fillRect(0,0,W,H); ctx.fillStyle="#fff";ctx.textAlign="center";ctx.font="bold 26px sans-serif";ctx.fillText(won?"Cleared!":"Game over",W/2,H/2); }
        raf=requestAnimationFrame(step);
      }
      step();
      s.append(hEl('<div class="note">Drag to move the paddle, tap to launch. Break all the bricks!</div>'));
      return ()=>cancelAnimationFrame(raf);
    }});

  // ---- Flappy Tap ----
  games.push({ id:"flappy", name:"Flappy Tap", tag:"Fly through the gaps", emoji:"🐤", color:"#f2c14e", cat:"Arcade",
    render(s){ const st=mk("div","status"); s.appendChild(st); const c=makeCanvas(s,340,500); if(!c.ctx)return; const {ctx,W,H,cv}=c;
      const gap=150, pw=54, R=14; let bird,pipes,score,over,started,spawn,raf;
      function reset(){ bird={y:H/2,v:0}; pipes=[]; score=0; over=false; started=false; spawn=0; }
      reset();
      cv.addEventListener("pointerdown",()=>{ if(over){reset();return;} started=true; bird.v=-6.2; FX.tap(); });
      function step(){ const g=ctx.createLinearGradient(0,0,0,H); g.addColorStop(0,"#1a1530"); g.addColorStop(1,"#0a0812"); ctx.fillStyle=g; ctx.fillRect(0,0,W,H);
        if(started&&!over){ bird.v+=0.35; bird.y+=bird.v; spawn--; if(spawn<=0){ pipes.push({x:W,top:50+rnd(H-gap-160),scored:false}); spawn=95; }
          pipes.forEach(p=>p.x-=2.5); pipes=pipes.filter(p=>p.x>-pw);
          pipes.forEach(p=>{ if(!p.scored&&p.x+pw<W/2-R){p.scored=true;score++;FX.pop();}
            if(W/2+R>p.x&&W/2-R<p.x+pw&&(bird.y-R<p.top||bird.y+R>p.top+gap)){over=true;FX.buzz();} });
          if(bird.y>H-R||bird.y<R){over=true;FX.buzz();Store.submitBest("flappy",score);} }
        pipes.forEach(p=>{ ctx.save();ctx.shadowBlur=10;ctx.shadowColor="#2fbf71";ctx.fillStyle="#2fbf71"; ctx.fillRect(p.x,0,pw,p.top); ctx.fillRect(p.x,p.top+gap,pw,H-p.top-gap); ctx.restore(); });
        ctx.save();ctx.shadowBlur=16;ctx.shadowColor="#f2c14e";ctx.fillStyle="#f2c14e";ctx.beginPath();ctx.arc(W/2,bird.y,R,0,TAU);ctx.fill();ctx.restore();
        ctx.fillStyle="#fff";ctx.font="bold 30px sans-serif";ctx.textAlign="center";ctx.fillText(score,W/2,58);
        st.textContent= over?("Score "+score+" · tap to retry"):started?("Score "+score):"Tap to start";
        if(over){ctx.fillStyle="rgba(0,0,0,.5)";ctx.fillRect(0,0,W,H);ctx.fillStyle="#fff";ctx.fillText("tap to retry",W/2,H/2);}
        raf=requestAnimationFrame(step); }
      step(); return ()=>cancelAnimationFrame(raf);
    }});

  // ---- Dash Run (endless runner) ----
  games.push({ id:"dashrun", name:"Dash Run", tag:"Jump obstacles · endless", emoji:"🏃", color:"#e5484d", cat:"Arcade",
    render(s){ const st=mk("div","status"); s.appendChild(st); const c=makeCanvas(s,340,420); if(!c.ctx)return; const {ctx,W,H,cv}=c;
      const groundY=H-60, R=16, px=70; let py,vy,jumps,obs,coins,speed,over,spawn,cspawn,dist,raf;
      function reset(){ py=groundY;vy=0;jumps=0;obs=[];coins=[];speed=4;over=false;spawn=0;cspawn=30;dist=0; }
      reset();
      cv.addEventListener("pointerdown",()=>{ if(over){reset();return;} if(jumps<2){vy=-9;jumps++;FX.tap();} });
      function step(){ const g=ctx.createLinearGradient(0,0,0,H);g.addColorStop(0,"#2a2450");g.addColorStop(1,"#0a0812");ctx.fillStyle=g;ctx.fillRect(0,0,W,H);
        ctx.strokeStyle="#4c7df0";ctx.lineWidth=3;ctx.beginPath();ctx.moveTo(0,groundY+R);ctx.lineTo(W,groundY+R);ctx.stroke();
        let score=Math.floor(dist/10);
        if(!over){ dist++; if(dist%600===0)speed+=0.5; vy+=0.5; py+=vy; if(py>groundY){py=groundY;vy=0;jumps=0;}
          spawn--; if(spawn<=0){ obs.push({x:W,h:24+rnd(30)}); spawn=Math.max(48,95-speed*4); }
          cspawn--; if(cspawn<=0){ coins.push({x:W,y:groundY-40-rnd(70)}); cspawn=40+rnd(40); }
          obs.forEach(o=>o.x-=speed); coins.forEach(o=>o.x-=speed); obs=obs.filter(o=>o.x>-30); coins=coins.filter(o=>o.x>-20);
          obs.forEach(o=>{ if(Math.abs(o.x-px)<R+12 && py>groundY-o.h-R+8){ over=true; FX.buzz(); Store.submitBest("dashrun",Math.floor(dist/10)); } });
          for(let i=coins.length-1;i>=0;i--){ if(Math.hypot(coins[i].x-px,coins[i].y-py)<R+12){ coins.splice(i,1); dist+=50; FX.pop(); } }
          score=Math.floor(dist/10);
        }
        obs.forEach(o=>{ ctx.save();ctx.shadowBlur=8;ctx.shadowColor="#e5484d";ctx.fillStyle="#e5484d"; roundRect(ctx,o.x-12,groundY+R-o.h,24,o.h,4);ctx.fill();ctx.restore(); });
        coins.forEach(o=>{ ctx.save();ctx.shadowBlur=10;ctx.shadowColor="#f2c14e";ctx.fillStyle="#f2c14e";ctx.beginPath();ctx.arc(o.x,o.y,8,0,TAU);ctx.fill();ctx.restore(); });
        ctx.save();ctx.shadowBlur=16;ctx.shadowColor="#2fbf71";ctx.fillStyle="#2fbf71";ctx.beginPath();ctx.arc(px,py,R,0,TAU);ctx.fill();ctx.restore();
        ctx.fillStyle="#fff";ctx.font="bold 20px sans-serif";ctx.textAlign="left";ctx.fillText("★ "+score,12,30);
        st.textContent= over?("Score "+score+" · tap to retry"):"Tap to jump · double-tap = double jump";
        if(over){ctx.fillStyle="rgba(0,0,0,.5)";ctx.fillRect(0,0,W,H);ctx.fillStyle="#fff";ctx.textAlign="center";ctx.font="bold 26px sans-serif";ctx.fillText("Score "+score,W/2,H/2);}
        raf=requestAnimationFrame(step); }
      step(); return ()=>cancelAnimationFrame(raf);
    }});

  // ---- Space Shooter ----
  games.push({ id:"shooter", name:"Space Shooter", tag:"Blast the invaders", emoji:"🚀", color:"#4c7df0", cat:"Arcade",
    render(s){ const st=mk("div","status"); s.appendChild(st); const c=makeCanvas(s,340,520); if(!c.ctx)return; const {ctx,W,H,cv,toLocal}=c;
      let ship,bul,foes,parts,score,lives,over,fire,spawn,raf;
      function reset(){ ship={x:W/2}; bul=[]; foes=[]; parts=[]; score=0; lives=3; over=false; fire=0; spawn=0; }
      reset();
      cv.addEventListener("pointerdown",e=>{ if(over){reset();return;} move(e); }); cv.addEventListener("pointermove",move);
      function move(e){ const l=toLocal(e.clientX,e.clientY); ship.x=Math.max(16,Math.min(W-16,l.x)); if(e.preventDefault)e.preventDefault(); }
      function boom(x,y){ for(let k=0;k<10;k++)parts.push({x,y,vx:(Math.random()-.5)*6,vy:(Math.random()-.5)*6,life:18,col:["#f2c14e","#e5484d","#fff"][rnd(3)]}); }
      function step(){ ctx.fillStyle="#0a0812";ctx.fillRect(0,0,W,H);
        ctx.fillStyle="rgba(255,255,255,.2)"; for(let i=0;i<22;i++){ ctx.fillRect((i*53)%W,(i*89+Date.now()/16)%H,2,2); }
        if(!over){ fire--; if(fire<=0){ bul.push({x:ship.x,y:H-42}); fire=13; FX.hit(); }
          spawn--; if(spawn<=0){ foes.push({x:20+rnd(W-40),y:-20,vy:1+Math.random()*1.5}); spawn=Math.max(22,58-score/40); }
          bul.forEach(b=>b.y-=7); bul=bul.filter(b=>b.y>-10); foes.forEach(f=>f.y+=f.vy);
          for(let fi=foes.length-1;fi>=0;fi--){ const f=foes[fi]; let hitI=-1; for(let bi=0;bi<bul.length;bi++){ if(Math.abs(f.x-bul[bi].x)<18&&Math.abs(f.y-bul[bi].y)<18){hitI=bi;break;} }
            if(hitI>=0){ boom(f.x,f.y); foes.splice(fi,1); bul.splice(hitI,1); score+=10; FX.pop(); continue; }
            if(f.y>H+20){ foes.splice(fi,1); lives--; FX.buzz(); if(lives<=0){over=true;Store.submitBest("shooter",score);} } }
        }
        parts.forEach(p=>{p.x+=p.vx;p.y+=p.vy;p.life--;ctx.globalAlpha=Math.max(0,p.life/18);ctx.fillStyle=p.col;ctx.fillRect(p.x,p.y,3,3);});ctx.globalAlpha=1;parts=parts.filter(p=>p.life>0);
        ctx.save();ctx.shadowBlur=8;ctx.shadowColor="#f2c14e";ctx.fillStyle="#f2c14e";bul.forEach(b=>ctx.fillRect(b.x-2,b.y-9,4,11));ctx.restore();
        ctx.font="24px sans-serif";ctx.textAlign="center";foes.forEach(f=>ctx.fillText("👾",f.x,f.y));
        ctx.font="26px sans-serif";ctx.fillText("🚀",ship.x,H-18);
        ctx.fillStyle="#fff";ctx.font="bold 18px sans-serif";ctx.textAlign="left";ctx.fillText("★ "+score,12,28);ctx.textAlign="right";ctx.fillText("❤".repeat(Math.max(0,lives)),W-12,28);
        st.textContent= over?("Score "+score+" · tap to retry"):"Drag to move — auto-fire";
        if(over){ctx.fillStyle="rgba(0,0,0,.55)";ctx.fillRect(0,0,W,H);ctx.fillStyle="#fff";ctx.textAlign="center";ctx.font="bold 26px sans-serif";ctx.fillText("Score "+score,W/2,H/2);}
        raf=requestAnimationFrame(step); }
      step(); return ()=>cancelAnimationFrame(raf);
    }});

  // ---- Sky Hopper (doodle-jump style) ----
  games.push({ id:"jumper", name:"Sky Hopper", tag:"Bounce as high as you can", emoji:"🦘", color:"#2fbf71", cat:"Arcade",
    render(s){ const st=mk("div","status"); s.appendChild(st); const c=makeCanvas(s,340,520); if(!c.ctx)return; const {ctx,W,H,cv,toLocal}=c;
      let px,py,vy,plats,score,over,dir,raf;
      function reset(){ px=W/2;py=H-80;vy=-9;score=0;over=false;dir=0; plats=[]; for(let i=0;i<8;i++)plats.push({x:rnd(W-60)+30,y:H-40-i*60}); }
      reset();
      function setDir(e){ const l=toLocal(e.clientX,e.clientY); dir=l.x<W/2?-1:1; if(e.preventDefault)e.preventDefault(); }
      cv.addEventListener("pointerdown",e=>{ if(over){reset();return;} setDir(e); });
      cv.addEventListener("pointermove",e=>{ if(dir!==0)setDir(e); });
      cv.addEventListener("pointerup",()=>{dir=0;}); cv.addEventListener("pointercancel",()=>{dir=0;});
      function step(){ const g=ctx.createLinearGradient(0,0,0,H);g.addColorStop(0,"#132a3a");g.addColorStop(1,"#0a0812");ctx.fillStyle=g;ctx.fillRect(0,0,W,H);
        if(!over){ px+=dir*4.5; if(px<0)px=W; if(px>W)px=0; vy+=0.35; py+=vy;
          if(py<H/2){ const dy=H/2-py; py=H/2; plats.forEach(p=>p.y+=dy); score+=Math.floor(dy); }
          plats.forEach(p=>{ if(vy>0&&px>p.x-32&&px<p.x+32&&py+15>p.y&&py+15<p.y+16){ vy=-10; FX.tap(); } });
          plats.forEach(p=>{ if(p.y>H){ p.y-=H+rnd(60); p.x=rnd(W-60)+30; } });
          if(py>H+20){over=true;FX.buzz();Store.submitBest("jumper",score);}
        }
        plats.forEach(p=>{ ctx.save();ctx.shadowBlur=8;ctx.shadowColor="#2fbf71";ctx.fillStyle="#2fbf71"; roundRect(ctx,p.x-32,p.y,64,12,6);ctx.fill();ctx.restore(); });
        ctx.save();ctx.shadowBlur=14;ctx.shadowColor="#f2c14e";ctx.fillStyle="#f2c14e";ctx.beginPath();ctx.arc(px,py,15,0,TAU);ctx.fill();ctx.restore();
        ctx.fillStyle="#fff";ctx.font="bold 20px sans-serif";ctx.textAlign="left";ctx.fillText("↑ "+score,12,28);
        st.textContent= over?("Height "+score+" · tap to retry"):"Hold left / right to steer";
        if(over){ctx.fillStyle="rgba(0,0,0,.55)";ctx.fillRect(0,0,W,H);ctx.fillStyle="#fff";ctx.textAlign="center";ctx.font="bold 26px sans-serif";ctx.fillText("Height "+score,W/2,H/2);}
        raf=requestAnimationFrame(step); }
      step(); return ()=>cancelAnimationFrame(raf);
    }});

  // ---- Pong Duel (2P) ----
  games.push({ id:"pong", name:"Pong Duel", tag:"Neon paddles · 2P", emoji:"🏓", color:"#b06bff", cat:"Competitive",
    render(s){ const st=mk("div","status"); st.textContent="0 — 0"; s.appendChild(st); const c=makeCanvas(s,340,540); if(!c.ctx)return; const {ctx,W,H,cv,toLocal}=c;
      const pw=72,ph=12,R=9; let top=W/2,bot=W/2,ball,s1=0,s2=0,over=false,raf; const ptr={};
      function serve(d){ ball={x:W/2,y:H/2,vx:(Math.random()-.5)*4,vy:d*4}; } serve(1);
      cv.addEventListener("pointerdown",e=>{ const l=toLocal(e.clientX,e.clientY); ptr[e.pointerId]=l.y>H/2?"b":"t"; mv(e); });
      cv.addEventListener("pointermove",mv); cv.addEventListener("pointerup",e=>{delete ptr[e.pointerId];}); cv.addEventListener("pointercancel",e=>{delete ptr[e.pointerId];});
      function mv(e){ const w=ptr[e.pointerId]; if(!w)return; const l=toLocal(e.clientX,e.clientY); const x=Math.max(pw/2,Math.min(W-pw/2,l.x)); if(w==="t")top=x; else bot=x; if(e.preventDefault)e.preventDefault(); }
      function glow(x,y,col){ ctx.save();ctx.shadowBlur=14;ctx.shadowColor=col;ctx.fillStyle=col; roundRect(ctx,x-pw/2,y-ph/2,pw,ph,6);ctx.fill();ctx.restore(); }
      function step(){ if(over)return; ball.x+=ball.vx; ball.y+=ball.vy;
        if(ball.x<R){ball.x=R;ball.vx*=-1;} if(ball.x>W-R){ball.x=W-R;ball.vx*=-1;}
        if(ball.y<24+R&&ball.vy<0&&Math.abs(ball.x-top)<pw/2){ ball.vy=Math.abs(ball.vy); ball.vx+=(ball.x-top)/12; FX.hit(); }
        if(ball.y>H-24-R&&ball.vy>0&&Math.abs(ball.x-bot)<pw/2){ ball.vy=-Math.abs(ball.vy); ball.vx+=(ball.x-bot)/12; FX.hit(); }
        if(ball.y<0){ s2++; st.textContent=s2+" — "+s1; FX.buzz(); serve(1); } if(ball.y>H){ s1++; st.textContent=s2+" — "+s1; FX.buzz(); serve(-1); }
        ctx.fillStyle="#0a0812";ctx.fillRect(0,0,W,H); ctx.strokeStyle="rgba(255,255,255,.12)";ctx.setLineDash([6,8]);ctx.beginPath();ctx.moveTo(0,H/2);ctx.lineTo(W,H/2);ctx.stroke();ctx.setLineDash([]);
        glow(top,22,"#4c7df0"); glow(bot,H-22,"#e5484d");
        ctx.save();ctx.shadowBlur=16;ctx.shadowColor="#fff";ctx.fillStyle="#fff";ctx.beginPath();ctx.arc(ball.x,ball.y,R,0,TAU);ctx.fill();ctx.restore();
        if(s1>=7||s2>=7){over=true;st.textContent=(s1>=7?"Bottom player wins!":"Top player wins!")+" 🎉";FX.win();return;}
        raf=requestAnimationFrame(step); }
      step();
      s.append(hEl('<div class="note">Top &amp; bottom players each drag their paddle. First to 7 — multi-touch.</div>'));
      return ()=>{over=true;cancelAnimationFrame(raf);};
    }});

  // ---- Dots & Boxes (2P) ----
  games.push({ id:"dotsboxes", name:"Dots & Boxes", tag:"Close boxes · 2P", emoji:"⬛", color:"#f2c14e", cat:"Competitive",
    render(s){ const N=4; const st=mk("div","status"); s.appendChild(st); const c=makeCanvas(s,340,340); if(!c.ctx)return; const {ctx,W,H,cv,toLocal}=c;
      const pad=32, gap=(W-2*pad)/N; let hE={},vE={},owner={},turn=1;
      const key=(r,cc)=>r+"_"+cc;
      const boxDone=(r,cc)=>hE[key(r,cc)]&&hE[key(r+1,cc)]&&vE[key(r,cc)]&&vE[key(r,cc+1)];
      const done=()=>{ for(let r=0;r<=N;r++)for(let cc=0;cc<N;cc++)if(!hE[key(r,cc)])return false; for(let r=0;r<N;r++)for(let cc=0;cc<=N;cc++)if(!vE[key(r,cc)])return false; return true; };
      cv.addEventListener("pointerdown",e=>{ const l=toLocal(e.clientX,e.clientY); tap(l.x,l.y); });
      function tap(x,y){ if(done())return; let best=null,bd=20;
        for(let r=0;r<=N;r++)for(let cc=0;cc<N;cc++){ if(hE[key(r,cc)])continue; const mx=pad+cc*gap+gap/2,my=pad+r*gap; const d=Math.hypot(x-mx,y-my); if(d<bd){bd=d;best={t:"h",r,c:cc};} }
        for(let r=0;r<N;r++)for(let cc=0;cc<=N;cc++){ if(vE[key(r,cc)])continue; const mx=pad+cc*gap,my=pad+r*gap+gap/2; const d=Math.hypot(x-mx,y-my); if(d<bd){bd=d;best={t:"v",r,c:cc};} }
        if(!best)return; if(best.t==="h")hE[key(best.r,best.c)]=true; else vE[key(best.r,best.c)]=true;
        let gained=0; for(let r=0;r<N;r++)for(let cc=0;cc<N;cc++){ if(!owner[key(r,cc)]&&boxDone(r,cc)){owner[key(r,cc)]=turn;gained++;} }
        if(gained)FX.pop(); else turn=turn===1?2:1; draw(); }
      function draw(){ ctx.fillStyle="#0a0812";ctx.fillRect(0,0,W,H); let s1=0,s2=0;
        for(let r=0;r<N;r++)for(let cc=0;cc<N;cc++){ const o=owner[key(r,cc)]; if(o){ if(o===1)s1++;else s2++; ctx.fillStyle=o===1?"rgba(229,72,77,.45)":"rgba(76,125,240,.45)"; ctx.fillRect(pad+cc*gap+3,pad+r*gap+3,gap-6,gap-6); } }
        ctx.lineWidth=5;ctx.lineCap="round";
        for(let r=0;r<=N;r++)for(let cc=0;cc<N;cc++){ ctx.strokeStyle=hE[key(r,cc)]?"#f3eefb":"rgba(255,255,255,.12)"; ctx.beginPath();ctx.moveTo(pad+cc*gap,pad+r*gap);ctx.lineTo(pad+cc*gap+gap,pad+r*gap);ctx.stroke(); }
        for(let r=0;r<N;r++)for(let cc=0;cc<=N;cc++){ ctx.strokeStyle=vE[key(r,cc)]?"#f3eefb":"rgba(255,255,255,.12)"; ctx.beginPath();ctx.moveTo(pad+cc*gap,pad+r*gap);ctx.lineTo(pad+cc*gap,pad+r*gap+gap);ctx.stroke(); }
        ctx.fillStyle="#a99fc0"; for(let r=0;r<=N;r++)for(let cc=0;cc<=N;cc++){ ctx.beginPath();ctx.arc(pad+cc*gap,pad+r*gap,4,0,TAU);ctx.fill(); }
        st.textContent = done()? (s1>s2?`Player 1 wins ${s1}–${s2}! 🎉`:s2>s1?`Player 2 wins ${s2}–${s1}! 🎉`:`Tie ${s1}–${s2}`) : `P1 ${s1} · P2 ${s2} · Player ${turn}'s turn`; }
      const btn=mk("button","btn","New game"); btn.onclick=()=>{hE={};vE={};owner={};turn=1;draw();};
      s.appendChild(btn); draw();
      s.append(hEl('<div class="note">Tap between two dots to draw a line. Close a box to score and go again.</div>'));
    }});

  // ---- Splash Duel (live territory paint, 2P) — original ----
  games.push({ id:"splash", name:"Splash Duel", tag:"Paint more territory · 2P live", emoji:"🎨", color:"#b06bff", cat:"Competitive",
    render(s){ const st=mk("div","status"); s.appendChild(st); const GW=9,GH=14; const c=makeCanvas(s,GW*30,GH*30); if(!c.ctx)return; const {ctx,W,H,cv,toLocal}=c;
      const cs=W/GW; let cells=new Array(GW*GH).fill(0), time=45, over=false, last=performance.now(), acc=0, raf; const ptr={};
      function paint(x,y,who){ const cx=Math.floor(x/cs),cy=Math.floor(y/cs); [[0,0],[1,0],[-1,0],[0,1],[0,-1]].forEach(([dx,dy])=>{ const nx=cx+dx,ny=cy+dy; if(nx>=0&&nx<GW&&ny>=0&&ny<GH)cells[ny*GW+nx]=who; }); }
      cv.addEventListener("pointerdown",e=>{ if(over){ cells=new Array(GW*GH).fill(0); time=45; over=false; acc=0; last=performance.now(); return; } const l=toLocal(e.clientX,e.clientY); ptr[e.pointerId]=l.y>H/2?1:2; paint(l.x,l.y,ptr[e.pointerId]); FX.tap(); });
      cv.addEventListener("pointermove",e=>{ const w=ptr[e.pointerId]; if(!w||over)return; const l=toLocal(e.clientX,e.clientY); paint(l.x,l.y,w); });
      cv.addEventListener("pointerup",e=>{delete ptr[e.pointerId];}); cv.addEventListener("pointercancel",e=>{delete ptr[e.pointerId];});
      function step(now){ now=now||performance.now(); const dt=now-last; last=now; if(!over){acc+=dt; if(acc>=1000){acc-=1000;time--; if(time<=0){over=true;FX.win();}}}
        ctx.clearRect(0,0,W,H);
        for(let i=0;i<cells.length;i++){ const o=cells[i]; ctx.fillStyle=o===1?"#e5484d":o===2?"#4c7df0":"#1c1830"; ctx.fillRect((i%GW)*cs+1,Math.floor(i/GW)*cs+1,cs-2,cs-2); }
        ctx.strokeStyle="rgba(255,255,255,.2)";ctx.lineWidth=2;ctx.beginPath();ctx.moveTo(0,H/2);ctx.lineTo(W,H/2);ctx.stroke();
        const c1=cells.filter(v=>v===1).length, c2=cells.filter(v=>v===2).length;
        st.textContent = over? (c1>c2?`🔴 wins ${c1}–${c2}! 🎉`:c2>c1?`🔵 wins ${c2}–${c1}! 🎉`:`Tie ${c1}–${c2}`) : `🔴 ${c1}  🔵 ${c2}  · ${time}s`;
        if(over){ ctx.fillStyle="rgba(0,0,0,.45)";ctx.fillRect(0,0,W,H);ctx.fillStyle="#fff";ctx.textAlign="center";ctx.font="bold 20px sans-serif";ctx.fillText("tap to rematch",W/2,H/2); }
        raf=requestAnimationFrame(step); }
      step();
      s.append(hEl('<div class="note">Bottom player is 🔴, top is 🔵. Smear your colour across the grid — most tiles when time runs out wins. Multi-touch, play at once!</div>'));
      return ()=>cancelAnimationFrame(raf);
    }});

  // ---- Orbit Sumo (physics ring-out, 2P) ----
  games.push({ id:"sumo", name:"Orbit Sumo", tag:"Shove them out the ring · 2P", emoji:"⭕", color:"#e5484d", cat:"Competitive",
    render(s){ const st=mk("div","status"); st.textContent="0 — 0"; s.appendChild(st); const c=makeCanvas(s,340,440); if(!c.ctx)return; const {ctx,W,H,cv,toLocal}=c;
      const cx=W/2, cy=H/2, ring=Math.min(W,H)/2-14, R=24; let s1=0,s2=0,round=true,winner=0,raf; const ptr={};
      const d1={x:cx,y:cy+70,px:cx,py:cy+70}, d2={x:cx,y:cy-70,px:cx,py:cy-70};
      function reset(){ d1.x=cx;d1.y=cy+70;d1.px=d1.x;d1.py=d1.y; d2.x=cx;d2.y=cy-70;d2.px=d2.x;d2.py=d2.y; round=true; winner=0; }
      cv.addEventListener("pointerdown",e=>{ if(!round){reset();return;} const l=toLocal(e.clientX,e.clientY); ptr[e.pointerId]=l.y>cy?"d1":"d2"; drag(e); });
      cv.addEventListener("pointermove",drag); cv.addEventListener("pointerup",e=>{delete ptr[e.pointerId];}); cv.addEventListener("pointercancel",e=>{delete ptr[e.pointerId];});
      function drag(e){ const w=ptr[e.pointerId]; if(!w)return; const l=toLocal(e.clientX,e.clientY); const d=w==="d1"?d1:d2; d.x=l.x; d.y=l.y; if(e.preventDefault)e.preventDefault(); }
      function step(){ const v1={x:d1.x-d1.px,y:d1.y-d1.py}, v2={x:d2.x-d2.px,y:d2.y-d2.py};
        const dx=d1.x-d2.x, dy=d1.y-d2.y, dist=Math.hypot(dx,dy);
        if(dist<2*R&&dist>0){ const nx=dx/dist,ny=dy/dist,ov=2*R-dist; d1.x+=nx*ov/2;d1.y+=ny*ov/2;d2.x-=nx*ov/2;d2.y-=ny*ov/2; const imp=Math.abs((v1.x-v2.x)*nx+(v1.y-v2.y)*ny)*1.3+2; d1.x+=nx*imp;d1.y+=ny*imp;d2.x-=nx*imp;d2.y-=ny*imp; FX.hit(); }
        d1.px=d1.x;d1.py=d1.y;d2.px=d2.x;d2.py=d2.y;
        if(round){ if(Math.hypot(d1.x-cx,d1.y-cy)>ring+R){s2++;winner=2;round=false;FX.win();} else if(Math.hypot(d2.x-cx,d2.y-cy)>ring+R){s1++;winner=1;round=false;FX.win();} }
        ctx.fillStyle="#0a0812";ctx.fillRect(0,0,W,H); ctx.strokeStyle="rgba(255,255,255,.22)";ctx.lineWidth=4;ctx.beginPath();ctx.arc(cx,cy,ring,0,TAU);ctx.stroke();
        [[d1,"#e5484d"],[d2,"#4c7df0"]].forEach(([d,col])=>{ ctx.save();ctx.shadowBlur=18;ctx.shadowColor=col;ctx.fillStyle=col;ctx.beginPath();ctx.arc(d.x,d.y,R,0,TAU);ctx.fill();ctx.restore(); });
        st.textContent = winner? (winner===1?"🔴 wins the round!":"🔵 wins the round!") : s1+" — "+s2;
        if(!round){ ctx.fillStyle="rgba(0,0,0,.4)";ctx.fillRect(0,0,W,H);ctx.fillStyle="#fff";ctx.textAlign="center";ctx.font="bold 20px sans-serif";ctx.fillText("tap to rematch",cx,cy); }
        raf=requestAnimationFrame(step); }
      step();
      s.append(hEl('<div class="note">Drag your disc (🔴 bottom, 🔵 top), build speed, and slam your rival out of the ring. Multi-touch.</div>'));
      return ()=>cancelAnimationFrame(raf);
    }});

  // ---- Reactor Duel (quick-draw reflex, 2P best of 5) ----
  games.push({ id:"reactor", name:"Reactor Duel", tag:"First to tap on green · 2P", emoji:"🟢", color:"#2fbf71", cat:"Competitive",
    render(s){ const st=mk("div","status"); s.appendChild(st); let s1=0,s2=0,phase="ready",timer=null;
      const z2=mk("button","tapzone"); z2.style.height="150px"; const mid=mk("div","status"); mid.style.textAlign="center"; const z1=mk("button","tapzone"); z1.style.height="150px";
      function col(bg,msg){ z1.style.background=bg; z2.style.background=bg; mid.textContent=msg; }
      function arm(){ phase="armed"; col("#b23a48","Wait for GREEN…"); clearTimeout(timer); timer=setTimeout(()=>{ phase="go"; col("#2fbf71","TAP NOW!"); }, 1200+rnd(3000)); }
      function endRound(winner,foul){ phase="done"; clearTimeout(timer); if(winner===1)s1++; else s2++; st.textContent="🔵 "+s2+"   🔴 "+s1;
        if(s1>=5||s2>=5){ phase="match"; col("#2a2440",(s1>=5?"🔴 wins the match! 🎉":"🔵 wins the match! 🎉")); FX.win(); }
        else col("#2a2440",(foul?"False start — point to ":"Point to ")+(winner===1?"🔴":"🔵")+" · tap to continue"); }
      function tap(p){ if(phase==="match"){ s1=0;s2=0;st.textContent="🔵 0   🔴 0"; arm(); return; }
        if(phase==="ready"||phase==="done"){ arm(); return; }
        if(phase==="armed"){ endRound(p===1?2:1,true); FX.buzz(); return; }
        if(phase==="go"){ endRound(p,false); FX.pop(); return; } }
      z1.addEventListener("click",()=>tap(1)); z2.addEventListener("click",()=>tap(2));
      z2.textContent="Player 2 (top)"; z1.textContent="Player 1 (bottom)"; st.textContent="🔵 0   🔴 0"; col("#2a2440","Tap either side to start · first to 5");
      s.append(st,z2,mid,z1,hEl('<div class="note">When both panels turn GREEN, be first to tap YOUR side. Tap early and your rival scores. First to 5 wins.</div>'));
      return ()=>clearTimeout(timer);
    }});

  // ---- Stack (tower builder) ----
  games.push({ id:"stack", name:"Stack", tag:"Drop blocks, build the tower", emoji:"🏗️", color:"#4c7df0", cat:"Arcade",
    render(s){ const st=mk("div","status"); s.appendChild(st); const c=makeCanvas(s,320,480); if(!c.ctx)return; const {ctx,W,H,cv}=c; const bh=26;
      let blocks,aw,ax,dir,speed,over,score,raf;
      function reset(){ blocks=[{x:W/2-70,w:140}]; aw=140; ax=0; dir=1; speed=2.4; over=false; score=0; }
      reset();
      cv.addEventListener("pointerdown",()=>{ if(over){reset();return;} const prev=blocks[blocks.length-1]; const l=Math.max(ax,prev.x), r=Math.min(ax+aw,prev.x+prev.w), ov=r-l;
        if(ov<=0){ over=true; FX.buzz(); Store.submitBest("stack",score); return; } blocks.push({x:l,w:ov}); aw=ov; score++; speed=Math.min(6,speed+0.08); ax=dir>0?0:W-aw; FX.pop(); });
      function step(){ if(!over){ ax+=dir*speed; if(ax<0){ax=0;dir=1;} if(ax+aw>W){ax=W-aw;dir=-1;} }
        const scroll=Math.max(0, blocks.length*bh-(H-170)); ctx.fillStyle="#0a0812";ctx.fillRect(0,0,W,H);
        blocks.forEach((b,i)=>{ const y=H-40-i*bh+scroll; if(y<-bh||y>H)return; const hue=(i*24)%360; ctx.save();ctx.shadowBlur=8;ctx.shadowColor="hsl("+hue+",70%,60%)";ctx.fillStyle="hsl("+hue+",65%,55%)"; roundRect(ctx,b.x,y,b.w,bh-3,5);ctx.fill();ctx.restore(); });
        if(!over){ const y=H-40-blocks.length*bh+scroll; ctx.save();ctx.shadowBlur=12;ctx.shadowColor="#fff";ctx.fillStyle="#f3eefb"; roundRect(ctx,ax,y,aw,bh-3,5);ctx.fill();ctx.restore(); }
        ctx.fillStyle="#fff";ctx.font="bold 24px sans-serif";ctx.textAlign="center";ctx.fillText(score,W/2,44);
        st.textContent= over?("Tower "+score+" · best "+Store.best("stack")+" · tap to retry"):"Tap to drop — line them up";
        if(over){ctx.fillStyle="rgba(0,0,0,.5)";ctx.fillRect(0,0,W,H);ctx.fillStyle="#fff";ctx.fillText("Tower "+score,W/2,H/2);}
        raf=requestAnimationFrame(step); }
      step(); return ()=>cancelAnimationFrame(raf);
    }});

  // ---- Meteor Dodge ----
  games.push({ id:"meteor", name:"Meteor Dodge", tag:"Survive the falling rocks", emoji:"☄️", color:"#e5484d", cat:"Arcade",
    render(s){ const st=mk("div","status"); s.appendChild(st); const c=makeCanvas(s,340,480); if(!c.ctx)return; const {ctx,W,H,cv,toLocal}=c;
      let px,rocks,t,over,spawn,last,raf;
      function reset(){ px=W/2; rocks=[]; t=0; over=false; spawn=0; last=performance.now(); }
      reset();
      cv.addEventListener("pointerdown",e=>{ if(over){reset();return;} mv(e); }); cv.addEventListener("pointermove",e=>{ if(!over)mv(e); });
      function mv(e){ const l=toLocal(e.clientX,e.clientY); px=Math.max(16,Math.min(W-16,l.x)); if(e.preventDefault)e.preventDefault(); }
      function step(now){ now=now||performance.now(); const dt=now-last; last=now; ctx.fillStyle="#0a0812";ctx.fillRect(0,0,W,H);
        if(!over){ t+=dt; spawn--; if(spawn<=0){ rocks.push({x:16+rnd(W-32),y:-20,r:10+rnd(16),v:2.5+Math.random()*2+t/8000}); spawn=Math.max(10,26-t/1000); }
          rocks.forEach(r=>r.y+=r.v); rocks=rocks.filter(r=>r.y<H+30);
          rocks.forEach(r=>{ if(Math.hypot(r.x-px,r.y-(H-40))<r.r+15){ over=true; FX.buzz(); Store.submitBest("meteor",Math.floor(t/100)); } }); }
        rocks.forEach(r=>{ ctx.save();ctx.shadowBlur=10;ctx.shadowColor="#e5484d";ctx.fillStyle="#e5484d";ctx.beginPath();ctx.arc(r.x,r.y,r.r,0,TAU);ctx.fill();ctx.restore(); });
        ctx.save();ctx.shadowBlur=16;ctx.shadowColor="#2fbf71";ctx.fillStyle="#2fbf71";ctx.beginPath();ctx.arc(px,H-40,15,0,TAU);ctx.fill();ctx.restore();
        const score=Math.floor(t/100); ctx.fillStyle="#fff";ctx.font="bold 20px sans-serif";ctx.textAlign="left";ctx.fillText("⏱ "+score,12,30);
        st.textContent= over?("Survived "+score+" · best "+Store.best("meteor")+" · tap to retry"):"Drag to dodge the meteors";
        if(over){ctx.fillStyle="rgba(0,0,0,.5)";ctx.fillRect(0,0,W,H);ctx.fillStyle="#fff";ctx.textAlign="center";ctx.font="bold 26px sans-serif";ctx.fillText("Score "+score,W/2,H/2);}
        raf=requestAnimationFrame(step); }
      step(); return ()=>cancelAnimationFrame(raf);
    }});

  // ---- Aim Trainer ----
  games.push({ id:"aim", name:"Aim Trainer", tag:"Tap targets · 20 seconds", emoji:"🎯", color:"#f2c14e", cat:"Arcade",
    render(s){ const st=mk("div","status"); s.appendChild(st); const c=makeCanvas(s,340,440); if(!c.ctx)return; const {ctx,W,H,cv,toLocal}=c;
      let tx,ty,tr,score,time,over,last,acc,raf;
      function place(){ tr=18+rnd(16); tx=tr+rnd(W-2*tr); ty=tr+44+rnd(H-2*tr-54); }
      function reset(){ score=0; time=20; over=false; last=performance.now(); acc=0; place(); }
      reset();
      cv.addEventListener("pointerdown",e=>{ if(over){reset();return;} const l=toLocal(e.clientX,e.clientY); if(Math.hypot(l.x-tx,l.y-ty)<tr+6){ score++; FX.pop(); FX.haptic(6); place(); } else FX.buzz(); });
      function step(now){ now=now||performance.now(); const dt=now-last; last=now; if(!over){acc+=dt; if(acc>=1000){acc-=1000;time--; if(time<=0){over=true;FX.win();Store.submitBest("aim",score);}}}
        ctx.fillStyle="#0a0812";ctx.fillRect(0,0,W,H);
        if(!over){ ctx.save();ctx.shadowBlur=16;ctx.shadowColor="#f2c14e"; ctx.fillStyle="#f2c14e";ctx.beginPath();ctx.arc(tx,ty,tr,0,TAU);ctx.fill(); ctx.fillStyle="#0a0812";ctx.beginPath();ctx.arc(tx,ty,tr*0.55,0,TAU);ctx.fill(); ctx.fillStyle="#f2c14e";ctx.beginPath();ctx.arc(tx,ty,tr*0.24,0,TAU);ctx.fill(); ctx.restore(); }
        ctx.fillStyle="#fff";ctx.font="bold 20px sans-serif";ctx.textAlign="left";ctx.fillText("★ "+score,12,30);ctx.textAlign="right";ctx.fillText(time+"s",W-12,30);
        st.textContent= over?("Hits "+score+" · best "+Store.best("aim")+" · tap to retry"):"Tap the targets fast!";
        if(over){ctx.fillStyle="rgba(0,0,0,.55)";ctx.fillRect(0,0,W,H);ctx.fillStyle="#fff";ctx.textAlign="center";ctx.font="bold 26px sans-serif";ctx.fillText("Hits "+score,W/2,H/2);}
        raf=requestAnimationFrame(step); }
      step(); return ()=>cancelAnimationFrame(raf);
    }});

  // ---- Simon (memory) ----
  games.push({ id:"simon", name:"Simon", tag:"Repeat the glowing pattern", emoji:"🟩", color:"#2fbf71", cat:"Solo & Puzzle",
    render(s){ const st=mk("div","status"); s.appendChild(st);
      const cols=["#e5484d","#2fbf71","#4c7df0","#f2c14e"]; let seq=[],inp=0,phase="ready",best=0;
      const gridEl=mk("div"); gridEl.style.cssText="display:grid;grid-template-columns:1fr 1fr;gap:12px;margin-top:8px;";
      const pads=cols.map((col,i)=>{ const p=mk("button"); p.style.cssText="aspect-ratio:1;border:none;border-radius:20px;background:"+col+";opacity:.35;cursor:pointer;transition:opacity .12s;"; p.addEventListener("click",()=>tap(i)); gridEl.appendChild(p); return p; });
      const btn=mk("button","btn","Start");
      const flash=(i,ms)=>{ pads[i].style.opacity="1"; FX.pop(); setTimeout(()=>{pads[i].style.opacity=".35";}, ms||280); };
      async function show(){ phase="show"; st.textContent="Watch…"; await new Promise(r=>setTimeout(r,400)); for(const i of seq){ flash(i,380); await new Promise(r=>setTimeout(r,560)); } phase="input"; inp=0; st.textContent="Your turn — level "+seq.length; }
      function next(){ seq.push(rnd(4)); show(); }
      function tap(i){ if(phase!=="input")return; flash(i,160); if(i===seq[inp]){ inp++; if(inp===seq.length){ if(seq.length>best)best=seq.length; Store.submitBest("simon",best); next(); } } else { phase="over"; Store.submitBest("simon",seq.length-1); st.textContent="Oops! Reached level "+seq.length+" · best "+best; FX.buzz(); btn.textContent="Try again"; btn.style.display=""; } }
      btn.addEventListener("click",()=>{ seq=[]; btn.style.display="none"; next(); });
      st.textContent="Repeat the pattern — how far can you go?";
      s.append(st,gridEl,btn);
    }});

  // ---- Schulte Table ----
  games.push({ id:"schulte", name:"Schulte Table", tag:"Tap 1→25 as fast as you can", emoji:"🔢", color:"#b06bff", cat:"Solo & Puzzle",
    render(s){ const st=mk("div","status"); s.appendChild(st); let nxt,start,running;
      const gridEl=mk("div"); gridEl.style.cssText="display:grid;grid-template-columns:repeat(5,1fr);gap:6px;margin-top:8px;";
      function build(){ const order=shuffle([...Array(25)].map((_,i)=>i+1)); nxt=1; running=false; start=0; gridEl.innerHTML="";
        order.forEach(n=>{ const b=mk("button","cell",""+n); b.style.aspectRatio="1"; b.style.fontSize="19px"; b.addEventListener("click",()=>tap(n,b)); gridEl.appendChild(b); }); }
      function tap(n,b){ if(!running){ running=true; start=performance.now(); } if(n===nxt){ b.style.background="var(--teal)"; b.disabled=true; nxt++; FX.pop();
          if(nxt>25){ const t=parseFloat(((performance.now()-start)/1000).toFixed(1)); const bt=Store.get("schulte_best",0); const isBest=bt===0||t<bt; if(isBest)Store.set("schulte_best",t); st.textContent="Done in "+t+"s"+(isBest?" — new best! 🎉":" · best "+bt+"s"); FX.win(); } else st.textContent="Find "+nxt; } else FX.buzz(); }
      const btn=mk("button","btn","Shuffle & restart"); btn.addEventListener("click",()=>{ build(); st.textContent="Tap 1 to start the timer"; });
      build(); st.textContent="Tap 1 to start the timer"; s.append(st,gridEl,btn);
    }});

  function shuffle(a){ for(let i=a.length-1;i>0;i--){const j=rnd(i+1);[a[i],a[j]]=[a[j],a[i]];} return a; }

  // ---------- build hub (grouped into Kulfi-style categories) ----------
  const CAT = { ttt:"Competitive", c4:"Competitive", gomoku:"Competitive", reversi:"Competitive",
    rps:"Competitive", rx:"Competitive", tapwar:"Competitive", whack:"Competitive", hilo:"Competitive",
    mem:"Solo & Puzzle", g2048:"Solo & Puzzle", snake:"Solo & Puzzle",
    tot:"Conversation", wyr:"Conversation" };
  const ORDER = ["Arcade", "Competitive", "Conversation", "Co-op", "Solo & Puzzle", "More"];
  const catOf = (g) => g.cat || CAT[g.id] || "More";
  grid.className = ""; // it now holds category sections, each with its own sub-grid
  // Daily streak + coins + reward bar (addiction loop).
  (function statsBar() {
    const d = dailyCheck();
    const bar = document.createElement("div");
    bar.style.cssText = "display:flex;gap:8px;align-items:center;margin:2px 2px 4px;";
    const pill = (txt) => { const e = mk("div", null, txt); e.style.cssText = "background:var(--hi);border-radius:50px;padding:8px 14px;font-weight:800;font-size:14px;"; return e; };
    const sb = mk("button", null, "🏆 Scores"); sb.style.cssText = "margin-left:auto;background:var(--coral);color:#fff;border:none;border-radius:50px;padding:8px 16px;font-weight:800;cursor:pointer;font-size:14px;"; sb.addEventListener("click", openScoreboard);
    bar.append(pill("🔥 " + d.streak), pill("🪙 " + d.coins), sb);
    hub.insertBefore(bar, grid);
    if (d.reward > 0) {
      const rw = document.createElement("div");
      rw.style.cssText = "background:linear-gradient(90deg,rgba(242,193,78,.25),rgba(229,72,77,.15));border:1px solid rgba(242,193,78,.5);border-radius:14px;padding:10px 14px;margin:0 2px 10px;font-size:14px;";
      rw.innerHTML = "🎁 <b>Daily reward!</b> +" + d.reward + " coins · Day " + d.streak + " streak 🔥";
      hub.insertBefore(rw, grid);
    }
  })();
  function tileFor(g) {
    const t = mk("button", "tile"); t.onclick = () => openGame(g);
    const ic = mk("div", "ic", g.emoji); ic.style.background = g.color;
    const body = mk("div"); body.append(mk("div", "nm", g.name), mk("div", "tg", g.tag));
    t.append(ic, body); return t;
  }
  ORDER.forEach((cat) => {
    const list = games.filter((g) => catOf(g) === cat);
    if (!list.length) return;
    const head = mk("div", null, cat);
    head.style.cssText = "font-size:13px;font-weight:800;letter-spacing:.6px;text-transform:uppercase;color:var(--muted);margin:18px 4px 12px;";
    grid.appendChild(head);
    const sub = mk("div", "grid");
    list.forEach((g) => sub.appendChild(tileFor(g)));
    grid.appendChild(sub);
  });

  // ---------- tabs (Play / Music) ----------
  const tabs = document.querySelectorAll("#tabs .tab");
  const playView = document.getElementById("playView");
  const musicView = document.getElementById("musicView");
  const togetherView = document.getElementById("togetherView");
  tabs.forEach((t) => t.addEventListener("click", () => {
    tabs.forEach((x) => x.classList.toggle("on", x === t));
    const tab = t.dataset.tab;
    playView.classList.toggle("active", tab === "play");
    musicView.classList.toggle("active", tab === "music");
    if (togetherView) togetherView.classList.toggle("active", tab === "together");
    const mv = document.getElementById("moviesView"); if (mv) mv.classList.toggle("active", tab === "movies");
    // Avoid audio collisions on mobile (one media at a time): pause the film when leaving
    // Movies, and pause music when entering Movies. Music keeps playing during games.
    if (tab === "movies") { if (ytPlayer && ytPlayer.pauseVideo) { try { ytPlayer.pauseVideo(); } catch (e) {} } }
    else { const ov = document.querySelector("#ownWrap video"); if (ov) { try { ov.pause(); } catch (e) {} } }
  }));

  // ---------- music (YouTube embedded player) ----------
  const yt = document.getElementById("yt");
  const ytInput = document.getElementById("ytInput");
  const ytGo = document.getElementById("ytGo");
  const ytSave = document.getElementById("ytSave");
  const ytShare = document.getElementById("ytShare");
  const ltMsg = document.getElementById("ltMsg");
  const savedEl = document.getElementById("saved");
  const recentEl = document.getElementById("recent");
  let curId = "jfKfPfyJRdk", curList = null;
  // YouTube IFrame API — programmatic playback is far more reliable than URL autoplay.
  let ytPlayer = null, ytReady = false;
  window.onYouTubeIframeAPIReady = function () { try { ytPlayer = new YT.Player("yt", { events: { onReady: () => { ytReady = true; } } }); } catch (e) {} };
  (function loadYtApi() { if (window.YT && window.YT.Player) { window.onYouTubeIframeAPIReady(); return; } if (document.getElementById("ytapi")) return; const s = document.createElement("script"); s.id = "ytapi"; s.src = "https://www.youtube.com/iframe_api"; document.head.appendChild(s); })();
  function pushRecent(item) { if (!item.id) return; let l = Store.get("recent", []); l = l.filter((i) => i.id !== item.id); l.unshift(item); Store.set("recent", l.slice(0, 15)); renderRecent(); }
  function renderRecent() {
    if (!recentEl) return; const l = Store.get("recent", []); recentEl.innerHTML = "";
    if (!l.length) { const n = mk("div", "note", "Songs you play show up here."); n.style.marginTop = "4px"; recentEl.appendChild(n); return; }
    l.forEach((it) => { const b = mk("button", "station"); b.innerHTML = `<span class="e">${it.type === "list" ? "🎼" : "🎵"}</span><span><div class="t">${it.label}</div><div class="d">${it.type === "list" ? "Playlist" : "Song"}</div></span>`; b.addEventListener("click", () => { if (it.type === "list") loadList(it.id); else loadYt(it.id); }); recentEl.appendChild(b); });
  }
  const ytId = (v) => { v = (v || "").trim(); const m = v.match(/(?:v=|youtu\.be\/|embed\/|shorts\/)([A-Za-z0-9_-]{11})/); if (m) return m[1]; if (/^[A-Za-z0-9_-]{11}$/.test(v)) return v; return null; };
  const ytListId = (v) => { const m = (v || "").match(/[?&]list=([A-Za-z0-9_-]+)/); return m ? m[1] : null; };
  function loadYt(id, start) {
    curId = id; curList = null;
    if (ytReady && ytPlayer && ytPlayer.loadVideoById) {
      try { ytPlayer.loadVideoById({ videoId: id, startSeconds: start || 0 }); if (ytPlayer.playVideo) ytPlayer.playVideo(); }
      catch (e) { yt.src = "https://www.youtube.com/embed/" + id + "?playsinline=1&autoplay=1&rel=0&enablejsapi=1" + (start ? "&start=" + start : ""); }
    } else {
      yt.src = "https://www.youtube.com/embed/" + id + "?playsinline=1&autoplay=1&rel=0&enablejsapi=1" + (start ? "&start=" + start : "");
    }
    if (ltMsg) ltMsg.textContent = "";
    pushRecent({ type: "song", id, label: "Song " + id.slice(0, 6) });
  }
  function loadList(listId) {
    curList = listId; curId = null;
    if (ytReady && ytPlayer && ytPlayer.loadPlaylist) {
      try { ytPlayer.loadPlaylist({ list: listId, listType: "playlist", index: 0 }); if (ytPlayer.playVideo) ytPlayer.playVideo(); }
      catch (e) { yt.src = "https://www.youtube.com/embed/videoseries?list=" + listId + "&playsinline=1&autoplay=1&enablejsapi=1"; }
    } else {
      yt.src = "https://www.youtube.com/embed/videoseries?list=" + listId + "&playsinline=1&autoplay=1&enablejsapi=1";
    }
    if (ltMsg) ltMsg.textContent = "";
    pushRecent({ type: "list", id: listId, label: "Playlist " + listId.slice(0, 6) });
  }
  function playInput() {
    const v = (ytInput.value || "").trim();
    const list = ytListId(v); if (list) return loadList(list);
    const id = ytId(v); if (id) return loadYt(id);
    if (ltMsg) ltMsg.textContent = v ? "That doesn't look like a YouTube link or ID — paste a full youtube.com / youtu.be link." : "Paste a YouTube song or playlist link, then tap Play.";
  }
  ytGo.addEventListener("click", playInput);
  ytInput.addEventListener("keydown", (e) => { if (e.key === "Enter") playInput(); });
  function renderSaved() {
    const list = Store.get("saved", []); savedEl.innerHTML = "";
    if (!list.length) { const n = mk("div", "note", "Tap ★ Save to keep a song or playlist here."); n.style.marginTop = "4px"; savedEl.appendChild(n); return; }
    list.forEach((it, idx) => {
      const b = mk("button", "station");
      b.innerHTML = `<span class="e">${it.type === "list" ? "🎼" : "🎵"}</span><span style="flex:1"><div class="t">${it.label}</div><div class="d">${it.type === "list" ? "Playlist" : "Song"}</div></span>`;
      const x = mk("span", "", "✕"); x.style.cssText = "color:var(--muted);padding:0 6px;font-size:16px;";
      x.addEventListener("click", (ev) => { ev.stopPropagation(); const l = Store.get("saved", []); l.splice(idx, 1); Store.set("saved", l); renderSaved(); });
      b.appendChild(x);
      b.addEventListener("click", () => { if (it.type === "list") loadList(it.id); else loadYt(it.id); });
      savedEl.appendChild(b);
    });
  }
  ytSave.addEventListener("click", () => {
    const list = Store.get("saved", []);
    const item = curList ? { type: "list", id: curList, label: "Playlist " + curList.slice(0, 6) } : { type: "song", id: curId, label: "Song " + (curId || "").slice(0, 6) };
    if (!item.id) return;
    if (!list.some((i) => i.id === item.id)) { list.unshift(item); Store.set("saved", list.slice(0, 40)); renderSaved(); ltMsg.textContent = "Saved to My Music ★"; }
    else ltMsg.textContent = "Already in My Music";
  });
  // "Listen / jam together": share a link that opens the same song or playlist, in sync.
  ytShare.addEventListener("click", async () => {
    const base = location.origin + location.pathname;
    const link = curList ? (base + "?ltl=" + curList) : (base + "?lt=" + curId + "&t=" + Date.now());
    try {
      if (navigator.share) { await navigator.share({ title: "Jam together on Music_BG", url: link }); ltMsg.textContent = "Shared! You'll both hear the same thing."; }
      else { await navigator.clipboard.writeText(link); ltMsg.textContent = "Link copied — send it so you both hear the same track/playlist."; }
    } catch (e) { ltMsg.textContent = "Share this link: " + link; }
  });
  (function joinListen() {
    const q = new URLSearchParams(location.search);
    const lt = q.get("lt"), ltl = q.get("ltl");
    const goMusic = () => { const m = document.querySelector('[data-tab="music"]'); if (m) m.click(); };
    if (ltl && /^[A-Za-z0-9_-]+$/.test(ltl)) { loadList(ltl); goMusic(); if (ltMsg) ltMsg.textContent = "Joined a shared playlist 🎧"; }
    else if (lt && /^[A-Za-z0-9_-]{11}$/.test(lt)) { const t = Number(q.get("t")); const el = t ? Math.floor((Date.now() - t) / 1000) : 0; loadYt(lt, el > 0 && el < 36000 ? el : 0); goMusic(); if (ltMsg) ltMsg.textContent = "Joined a shared session — same spot 🎧"; }
  })();
  renderSaved();
  renderRecent();

  // ---------- Together: group video call (Jitsi) + watch party ----------
  const togetherTab = () => { const t = document.querySelector('[data-tab="together"]'); if (t) t.click(); };
  const roomInput = document.getElementById("roomInput");
  const callStart = document.getElementById("callStart");
  const callInvite = document.getElementById("callInvite");
  const jitsiWrap = document.getElementById("jitsiWrap");
  const wpInput = document.getElementById("wpInput");
  const wpGo = document.getElementById("wpGo");
  const wpShare = document.getElementById("wpShare");
  const wpFrame = document.getElementById("wpFrame");
  const tMsg = document.getElementById("tMsg");
  let curRoom = "", wpCur = "";
  const cleanRoom = (r) => (r || "").replace(/[^A-Za-z0-9_-]/g, "");
  function startCall(raw) {
    const r = cleanRoom(raw) || "party" + Math.random().toString(36).slice(2, 7);
    curRoom = r; jitsiWrap.style.display = "block";
    jitsiWrap.innerHTML = '<iframe allow="camera; microphone; fullscreen; display-capture; autoplay" src="https://meet.jit.si/MusicBG_' + encodeURIComponent(r) + '#config.prejoinPageEnabled=false"></iframe>';
  }
  if (callStart) callStart.addEventListener("click", () => { startCall(roomInput.value); tMsg.textContent = "Room: " + curRoom + " — tap Invite to bring friends."; });
  if (callInvite) callInvite.addEventListener("click", async () => {
    if (!curRoom) startCall(roomInput.value);
    const link = location.origin + location.pathname + "?room=" + encodeURIComponent(curRoom);
    try { if (navigator.share) await navigator.share({ title: "Join my Music_BG hangout", url: link }); else { await navigator.clipboard.writeText(link); tMsg.textContent = "Invite link copied!"; } } catch (e) { tMsg.textContent = "Invite: " + link; }
  });
  function wpLoad(id, start) { wpCur = id; wpFrame.src = "https://www.youtube.com/embed/" + id + "?playsinline=1&autoplay=1&rel=0" + (start ? "&start=" + start : ""); }
  if (wpGo) wpGo.addEventListener("click", () => { const id = ytId(wpInput.value); if (id) wpLoad(id); });
  if (wpShare) wpShare.addEventListener("click", async () => {
    if (!wpCur) return; const link = location.origin + location.pathname + "?wp=" + wpCur + "&t=" + Date.now();
    try { if (navigator.share) await navigator.share({ title: "Watch together on Music_BG", url: link }); else { await navigator.clipboard.writeText(link); tMsg.textContent = "Watch link copied — everyone sees the same spot."; } } catch (e) { tMsg.textContent = "Watch link: " + link; }
  });
  (function joinTogether() {
    const q = new URLSearchParams(location.search);
    const room = q.get("room"), wp = q.get("wp");
    if (room) { roomInput.value = cleanRoom(room); startCall(room); togetherTab(); tMsg.textContent = "Joined room " + cleanRoom(room) + " 👥"; }
    if (wp && /^[A-Za-z0-9_-]{11}$/.test(wp)) { const t = Number(q.get("t")); const el = t ? Math.floor((Date.now() - t) / 1000) : 0; wpLoad(wp, el > 0 && el < 36000 ? el : 0); if (!room) togetherTab(); }
  })();

  // ---------- Movies: TMDB catalog + watchlist + your-own-file player ----------
  (function movies() {
    const gate = document.getElementById("tmdbGate"), appEl = document.getElementById("tmdbApp");
    if (!gate) return;
    const keyInput = document.getElementById("tmdbKey"), keySave = document.getElementById("tmdbSave");
    const searchEl = document.getElementById("movieSearch"), searchBtn = document.getElementById("movieSearchBtn");
    const trendBtn = document.getElementById("movieTrending"), watchBtn = document.getElementById("movieWatch");
    const head = document.getElementById("movieHead"), grid = document.getElementById("movieGrid");
    const ownUrl = document.getElementById("ownUrl"), ownPlay = document.getElementById("ownPlay"), ownStop = document.getElementById("ownStop"), ownStatus = document.getElementById("ownStatus"), ownWrap = document.getElementById("ownWrap");
    let key = Store.get("tmdbKey", "");
    const IMG = (p) => p ? ("https://image.tmdb.org/t/p/w342" + p) : "";
    const watchlist = () => Store.get("watchlist", []);
    const inWatch = (id) => watchlist().some((m) => m.id === id);
    function toggleWatch(m) { let l = watchlist(); if (inWatch(m.id)) l = l.filter((x) => x.id !== m.id); else l.unshift({ id: m.id, title: m.title, poster: m.poster_path, year: (m.release_date || "").slice(0, 4), rating: m.vote_average }); Store.set("watchlist", l); }
    function showApp() { const has = !!key; gate.style.display = has ? "none" : "block"; appEl.style.display = has ? "block" : "none"; }
    showApp();
    async function tmdb(path) { const url = "https://api.themoviedb.org/3/" + path + (path.includes("?") ? "&" : "?") + "api_key=" + encodeURIComponent(key); const r = await fetch(url); if (!r.ok) throw new Error("TMDB " + r.status); return r.json(); }
    function renderGrid(list, label) {
      head.textContent = label || ""; grid.innerHTML = "";
      if (!list || !list.length) { head.textContent = (label || "") + " — nothing here yet."; return; }
      list.forEach((m) => {
        const poster = m.poster || m.poster_path; const rt = (m.rating != null ? m.rating : m.vote_average) || 0; const rtStr = typeof rt === "number" ? rt.toFixed(1) : rt; const yr = m.year || (m.release_date || "").slice(0, 4);
        const c = mk("button", "pc");
        c.innerHTML = (poster ? `<img src="${IMG(poster)}" alt="">` : `<div style="aspect-ratio:2/3;background:var(--hi)"></div>`) + `<div class="pt">${m.title || "?"}</div><div class="pr">★ ${rtStr}${yr ? " · " + yr : ""}</div>`;
        c.addEventListener("click", () => detail(m.id)); grid.appendChild(c);
      });
    }
    async function trending() { if (!key) return; try { head.textContent = "Loading trending…"; const d = await tmdb("trending/movie/week"); renderGrid(d.results || [], "🔥 Trending this week"); } catch (e) { head.textContent = "Couldn't load — check your TMDB key."; } }
    async function search(q) { if (!key || !q) return; try { head.textContent = "Searching…"; const d = await tmdb("search/movie?query=" + encodeURIComponent(q)); renderGrid(d.results || [], 'Results for "' + q + '"'); } catch (e) { head.textContent = "Search failed — check your key."; } }
    async function detail(id) {
      if (!key) return;
      try {
        const m = await tmdb("movie/" + id); grid.innerHTML = ""; head.textContent = "";
        const wrap = mk("div", "card");
        wrap.innerHTML = `<div style="display:flex;gap:12px">${m.poster_path ? `<img src="${IMG(m.poster_path)}" style="width:96px;border-radius:10px" alt="">` : ""}<div style="flex:1"><div style="font-weight:800;font-size:18px">${m.title}</div><div class="note" style="margin-top:2px">${(m.release_date || "").slice(0, 4)} · ★ ${(m.vote_average || 0).toFixed(1)} · ${m.runtime || "?"}m</div></div></div><div class="note" style="margin-top:10px">${m.overview || ""}</div>`;
        const row = mk("div", "row"); row.style.marginTop = "10px";
        const w = mk("button", "btn", inWatch(m.id) ? "★ In watchlist — remove" : "★ Add to watchlist"); w.style.marginTop = "0"; w.addEventListener("click", () => { toggleWatch(m); w.textContent = inWatch(m.id) ? "★ In watchlist — remove" : "★ Add to watchlist"; });
        const back = mk("button", "btn ghost", "Back"); back.style.marginTop = "0"; back.style.flex = "0 0 auto"; back.addEventListener("click", trending);
        row.append(w, back); wrap.appendChild(row); grid.appendChild(wrap);
      } catch (e) { head.textContent = "Couldn't load that title."; }
    }
    keySave.addEventListener("click", () => { const k = (keyInput.value || "").trim(); if (!k) return; key = k; Store.set("tmdbKey", k); showApp(); trending(); });
    searchBtn.addEventListener("click", () => search((searchEl.value || "").trim()));
    searchEl.addEventListener("keydown", (e) => { if (e.key === "Enter") search((searchEl.value || "").trim()); });
    trendBtn.addEventListener("click", trending);
    watchBtn.addEventListener("click", () => renderGrid(watchlist(), "★ Your watchlist"));
    // ---- Bhavay Video Player: direct HLS/MP4 engine (no P2P) ----
    let hls = null;
    function log(msg, isErr) { if (!ownStatus) return; ownStatus.textContent = msg; ownStatus.classList.toggle("err", !!isErr); }
    function loadHls() { return new Promise((res) => { if (window.Hls) return res(window.Hls); log("Loading HLS engine…"); const s = document.createElement("script"); s.src = "https://cdn.jsdelivr.net/npm/hls.js@1"; s.onload = () => res(window.Hls); s.onerror = () => res(null); document.head.appendChild(s); }); }
    function safePlay(v) { try { const p = v.play && v.play(); if (p && p.catch) p.catch((e) => log("Tap ▶ to start (autoplay blocked): " + (e && e.message || e), true)); } catch (e) { log("Tap ▶ to start playback.", true); } }
    function resetPlayer() {
      // Fully tear down the previous session (frees sockets/buffers), then mount a fresh <video> to avoid buffer lockups & memory leaks.
      if (hls) { try { hls.destroy(); } catch (e) {} hls = null; }
      ownWrap.innerHTML = "";
      const v = mk("video"); v.controls = true; v.autoplay = true; v.playsInline = true; v.setAttribute("playsinline", "");
      v.addEventListener("playing", () => log("Playing."));
      v.addEventListener("waiting", () => log("Buffering…"));
      v.addEventListener("ended", () => log("Finished."));
      v.addEventListener("error", () => log("Playback error — check the URL is reachable and CORS-enabled.", true));
      ownWrap.appendChild(v); ownWrap.style.display = "flex";
      return v;
    }
    async function playUrl(u) {
      if (!u) return log("Enter a direct .m3u8, .mp4 or .webm URL you own.", true);
      if (!/^https?:\/\//i.test(u)) return log("Only http(s) URLs are supported (no magnet / P2P links).", true);
      const video = resetPlayer();
      log("Resolving " + u.slice(0, 60) + (u.length > 60 ? "…" : ""));
      const isHls = /\.m3u8(\?|$)/i.test(u);
      const nativeHls = video.canPlayType && video.canPlayType("application/vnd.apple.mpegurl");
      if (isHls && !nativeHls) {
        const Hls = await loadHls();
        if (Hls && Hls.isSupported()) {
          hls = new Hls({ maxBufferLength: 30, maxMaxBufferLength: 600 });
          hls.loadSource(u); hls.attachMedia(video);
          hls.on(Hls.Events.MANIFEST_PARSED, () => { log("HLS manifest parsed — starting playback."); safePlay(video); });
          hls.on(Hls.Events.ERROR, (ev, data) => { if (data && data.fatal) log("HLS fatal error: " + data.type, true); });
          return;
        }
        log("HLS isn't supported in this browser.", true); return;
      }
      video.src = u;
      safePlay(video);
      log(isHls ? "Native HLS loaded." : "Direct file loaded.");
    }
    ownPlay.addEventListener("click", () => playUrl((ownUrl.value || "").trim()));
    if (ownStop) ownStop.addEventListener("click", () => { if (hls) { try { hls.destroy(); } catch (e) {} hls = null; } ownWrap.innerHTML = ""; ownWrap.style.display = "none"; log("Stopped."); });

    // ---- Internet Archive: free public-domain films, no API key (works for every user) ----
    const iaSearch = document.getElementById("iaSearch"), iaSearchBtn = document.getElementById("iaSearchBtn"), iaPopular = document.getElementById("iaPopular"), iaHead = document.getElementById("iaHead"), iaGrid = document.getElementById("iaGrid");
    const iaImg = (id) => "https://archive.org/services/img/" + id;
    async function iaJson(url) { if (typeof fetch !== "function") return null; const r = await fetch(url); if (!r.ok) throw new Error("IA " + r.status); return r.json(); }
    function iaRender(docs, label) {
      iaHead.textContent = label || ""; iaGrid.innerHTML = "";
      if (!docs || !docs.length) { iaHead.textContent = (label || "") + " — nothing found."; return; }
      docs.forEach((d) => {
        const c = mk("button", "pc"); const yr = d.year ? (" · " + d.year) : "";
        c.innerHTML = `<img src="${iaImg(d.identifier)}" alt="" loading="lazy">` + `<div class="pt">${d.title || d.identifier}</div><div class="pr">▶ play${yr}</div>`;
        c.addEventListener("click", () => iaPlay(d.identifier, d.title)); iaGrid.appendChild(c);
      });
    }
    async function iaQuery(q, label) {
      if (typeof fetch !== "function") return;
      try {
        iaHead.textContent = "Loading…";
        const url = "https://archive.org/advancedsearch.php?q=" + encodeURIComponent(q) + "&fl[]=identifier&fl[]=title&fl[]=year&sort[]=downloads+desc&rows=36&page=1&output=json";
        const d = await iaJson(url);
        iaRender((d && d.response && d.response.docs) || [], label);
      } catch (e) { iaHead.textContent = "Couldn't reach the Internet Archive right now."; }
    }
    function archiveBrowse(q) { if (q) iaQuery("mediatype:movies AND (" + q + ")", 'Results for "' + q + '"'); else iaQuery("collection:feature_films AND mediatype:movies", "🎞️ Popular free feature films"); }
    async function iaPlay(id, title) {
      if (typeof fetch !== "function") return;
      try {
        log("Finding a playable file for " + (title || id) + "…");
        const meta = await iaJson("https://archive.org/metadata/" + id);
        const files = (meta && meta.files) || [];
        const pick = files.find((f) => /\.mp4$/i.test(f.name) && /(512kb|h\.?264|mpeg4|mp4)/i.test((f.format || "") + f.name)) || files.find((f) => /\.mp4$/i.test(f.name)) || files.find((f) => /\.(webm|ogv)$/i.test(f.name));
        if (!pick) { log("No web-playable file in this title — try another.", true); return; }
        const src = "https://archive.org/download/" + id + "/" + encodeURIComponent(pick.name);
        if (ownUrl) ownUrl.value = src;
        playUrl(src);
        if (ownWrap && ownWrap.scrollIntoView) ownWrap.scrollIntoView({ behavior: "smooth", block: "center" });
      } catch (e) { log("Couldn't load that film — try another.", true); }
    }
    if (iaSearchBtn) iaSearchBtn.addEventListener("click", () => archiveBrowse((iaSearch.value || "").trim()));
    if (iaSearch) iaSearch.addEventListener("keydown", (e) => { if (e.key === "Enter") archiveBrowse((iaSearch.value || "").trim()); });
    if (iaPopular) iaPopular.addEventListener("click", () => archiveBrowse(""));
    // Lazy-load popular free films the first time the Movies tab is opened.
    let iaLoaded = false;
    const moviesTabBtn = document.querySelector('[data-tab="movies"]');
    if (moviesTabBtn) moviesTabBtn.addEventListener("click", () => { if (!iaLoaded) { iaLoaded = true; archiveBrowse(""); } });

    if (key) trending();
  })();
  const stations = [
    { e: "🎧", t: "Lofi hip hop radio", d: "beats to relax / study to", id: "jfKfPfyJRdk" },
    { e: "🌆", t: "Synthwave radio", d: "chill retro vibes", id: "4xDzrJKXOOY" },
  ];
  const stEl = document.getElementById("stations");
  stations.forEach((s) => {
    const b = mk("button", "station");
    b.innerHTML = `<span class="e">${s.e}</span><span><div class="t">${s.t}</div><div class="d">${s.d}</div></span>`;
    b.addEventListener("click", () => loadYt(s.id));
    stEl.appendChild(b);
  });

  // ---------- iOS install hint ----------
  (function iosHint() {
    const hint = document.getElementById("iosHint");
    if (!hint) return;
    const isIOS = /iP(hone|ad|od)/.test(navigator.userAgent) ||
      (navigator.platform === "MacIntel" && navigator.maxTouchPoints > 1);
    const standalone = window.navigator.standalone === true ||
      (window.matchMedia && window.matchMedia("(display-mode: standalone)").matches);
    if (isIOS && !standalone && localStorage.getItem("iosHintDismissed") !== "1") hint.style.display = "block";
    const x = document.getElementById("iosHintX");
    if (x) x.addEventListener("click", () => { hint.style.display = "none"; localStorage.setItem("iosHintDismissed", "1"); });
  })();

  // service worker for offline / installable
  if ("serviceWorker" in navigator) navigator.serviceWorker.register("sw.js").catch(() => {});
})();
