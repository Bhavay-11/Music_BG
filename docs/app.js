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
      s.append(st,wrap,H('<div class="note">Great for two — debate it, then tap one to move on.</div>')); round(); }});

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
      function start(){score=0;left=20;run=true;btn.style.display="none";moveT();clearInterval(t1);t1=setInterval(()=>{left--;draw();if(left<=0){run=false;clearInterval(t1);clearTimeout(t2);if(score>best)best=score;btn.textContent="Play again";btn.style.display="";draw();}},1000);draw();}
      btn.onclick=start;
      s.append(st,board,btn); draw();
      return ()=>{clearInterval(t1);clearTimeout(t2);}; }});

  // ---- deck-cycler helper for conversation games ----
  function deckGame(cfg){ games.push({ id:cfg.id, name:cfg.name, tag:cfg.tag, emoji:cfg.emoji, color:cfg.color, cat:"Conversation",
    render(s){ let order=shuffle(cfg.cards.map((_,i)=>i)), i=0;
      const st=mk("div","status"); const card=mk("div","card"); card.style.minHeight="180px";
      function show(){ card.innerHTML=""; st.textContent=`Card ${i+1}`; card.append(mk("div","center","",), mk("div","center big",cfg.emoji)); const q=mk("div","status",(cfg.prefix||"")+cfg.cards[order[i%order.length]]); q.style.textAlign="center"; q.style.fontWeight="700"; card.append(q); }
      const b=mk("button","btn","Next"); b.onclick=()=>{ i++; if(i%order.length===0) order=shuffle(order); show(); };
      s.append(st,card,b,H(`<div class="note">${cfg.note}</div>`)); show();
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
      s.append(st,card,seg,next,H('<div class="note">Pass-and-play on one phone.</div>')); render2();
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
      s.append(mk("div","status","Draw together"), cv, pal, clr, H('<div class="note">Both of you can draw — huddle up or pass the phone.</div>'));
    }});

  function shuffle(a){ for(let i=a.length-1;i>0;i--){const j=rnd(i+1);[a[i],a[j]]=[a[j],a[i]];} return a; }

  // ---------- build hub (grouped into Kulfi-style categories) ----------
  const CAT = { ttt:"Competitive", c4:"Competitive", gomoku:"Competitive", reversi:"Competitive",
    rps:"Competitive", rx:"Competitive", tapwar:"Competitive", whack:"Competitive", hilo:"Competitive",
    mem:"Solo & Puzzle", g2048:"Solo & Puzzle", snake:"Solo & Puzzle",
    tot:"Conversation", wyr:"Conversation" };
  const ORDER = ["Conversation", "Competitive", "Co-op", "Solo & Puzzle", "More"];
  const catOf = (g) => g.cat || CAT[g.id] || "More";
  grid.className = ""; // it now holds category sections, each with its own sub-grid
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
  tabs.forEach((t) => t.addEventListener("click", () => {
    tabs.forEach((x) => x.classList.toggle("on", x === t));
    const tab = t.dataset.tab;
    playView.classList.toggle("active", tab === "play");
    musicView.classList.toggle("active", tab === "music");
  }));

  // ---------- music (YouTube embedded player) ----------
  const yt = document.getElementById("yt");
  const ytInput = document.getElementById("ytInput");
  const ytGo = document.getElementById("ytGo");
  const ytId = (v) => { v = (v || "").trim(); const m = v.match(/(?:v=|youtu\.be\/|embed\/|shorts\/)([A-Za-z0-9_-]{11})/); if (m) return m[1]; if (/^[A-Za-z0-9_-]{11}$/.test(v)) return v; return null; };
  const loadYt = (id) => { yt.src = "https://www.youtube.com/embed/" + id + "?playsinline=1&autoplay=1&rel=0"; };
  ytGo.addEventListener("click", () => { const id = ytId(ytInput.value); if (id) loadYt(id); });
  ytInput.addEventListener("keydown", (e) => { if (e.key === "Enter") ytGo.click(); });
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
