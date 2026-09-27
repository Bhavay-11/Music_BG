"use strict";
(function () {
  const grid = document.getElementById("grid");
  const stage = document.getElementById("stage");
  const hub = document.getElementById("hub");
  const backBtn = document.getElementById("backBtn");
  const subtitle = document.getElementById("subtitle");
  let cleanup = null;

  const H = (html) => { const d = document.createElement("div"); d.innerHTML = html; return d; };

  function showHub() {
    if (cleanup) { cleanup(); cleanup = null; }
    stage.classList.remove("active"); stage.innerHTML = "";
    hub.classList.add("active");
    backBtn.classList.remove("show");
    subtitle.textContent = "Play together — near or far";
    document.title = "Music_BG · Play";
  }
  function openGame(g) {
    if (cleanup) { cleanup(); cleanup = null; }
    hub.classList.remove("active");
    stage.innerHTML = ""; stage.classList.add("active");
    backBtn.classList.add("show");
    subtitle.textContent = g.tag;
    cleanup = g.render(stage) || null;
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
          else { busy = true; setTimeout(() => { deck[first].up = c.up = false; first = null; busy = false; draw(); }, 700); } } }
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
      s.append(st, zone, H('<div class="note">Couple mode: pass the phone and see who is quicker.</div>'));
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
        st.textContent = movesLeft()? "Score: "+score : "Game over · "+score; }
      swipe(board,move);
      const dpad=H(`<div class="dpad"><div></div><button class="dbtn" data-d="up">▲</button><div></div>
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
        st.textContent = over? "Game over · "+score : "Score: "+score; }
      const DV={up:[-1,0],right:[0,1],down:[1,0],left:[0,-1]};
      let d="right";
      function tick(){ const [dr,dc]=DV[d]; const hr=Math.floor(snake[0]/N)+dr, hc=snake[0]%N+dc;
        if(hr<0||hr>=N||hc<0||hc>=N){over=true;run=false;clearInterval(loop);draw();return;}
        const nh=hr*N+hc; if(snake.includes(nh)){over=true;run=false;clearInterval(loop);draw();return;}
        const ate=nh===food; snake=[nh,...(ate?snake:snake.slice(0,-1))]; if(ate){score++; const free=[]; for(let i=0;i<N*N;i++)if(!snake.includes(i))free.push(i); food=free[rnd(free.length)]; } draw(); }
      function turn(nd){ const opp={up:"down",down:"up",left:"right",right:"left"}; if(nd===opp[d])return; d=nd; }
      function start(){ snake=[Math.floor(N*N/2)]; d="right"; food=Math.floor(N*N/2)+3; over=false; score=0; run=true; clearInterval(loop); loop=setInterval(tick,170); draw(); }
      swipe(board,turn);
      const dpad=H(`<div class="dpad"><div></div><button class="dbtn" data-d="up">▲</button><div></div>
        <button class="dbtn" data-d="left">◀</button><div></div><button class="dbtn" data-d="right">▶</button>
        <div></div><button class="dbtn" data-d="down">▼</button><div></div></div>`).firstChild;
      dpad.querySelectorAll(".dbtn").forEach(b=>b.onclick=()=>turn(b.dataset.d));
      const btn=mk("button","btn","Start / Restart"); btn.onclick=start;
      s.append(st,board,dpad,btn); draw();
      return ()=>clearInterval(loop);
    }});

  function shuffle(a){ for(let i=a.length-1;i>0;i--){const j=rnd(i+1);[a[i],a[j]]=[a[j],a[i]];} return a; }

  // ---------- build hub ----------
  games.forEach(g => {
    const t = mk("button", "tile"); t.onclick = () => openGame(g);
    const ic = mk("div", "ic", g.emoji); ic.style.background = g.color;
    const body = mk("div"); body.append(mk("div", "nm", g.name), mk("div", "tg", g.tag));
    t.append(ic, body); grid.appendChild(t);
  });

  // service worker for offline / installable
  if ("serviceWorker" in navigator) navigator.serviceWorker.register("sw.js").catch(()=>{});
})();
