'use strict';

/* =====================================================================
   BCNumber — утилиты для работы с числом
   ===================================================================== */
const BCNumber = {

  /** Генерирует корректное случайное число для игры. */
  generate(size) {
    const digits = [0,1,2,3,4,5,6,7,8,9];
    let result;
    do {
      for (let i = digits.length - 1; i > 0; i--) {
        const j = Math.floor(Math.random() * (i + 1));
        [digits[i], digits[j]] = [digits[j], digits[i]];
      }
      result = digits.slice(0, size).join('');
    } while (result[0] === '0');
    return result;
  },

  /** Количество быков (цифра угадана и стоит на своём месте). */
  bools(secret, guess) {
    let b = 0;
    for (let i = 0; i < secret.length; i++) {
      if (secret[i] === guess[i]) b++;
    }
    return b;
  },

  /** Количество коров (цифра угадана, но стоит не на своём месте). */
  cows(secret, guess) {
    let c = 0;
    for (let i = 0; i < guess.length; i++) {
      const idx = secret.indexOf(guess[i]);
      if (idx >= 0 && idx !== i) c++;
    }
    return c;
  },

  /** Проверяет корректность числа: нужная длина, нет ведущего нуля, все цифры разные. */
  isValid(s, size) {
    if (!s || s.length !== size || s[0] === '0') return false;
    for (let i = 0; i < size; i++) {
      if (!/\d/.test(s[i])) return false;
      for (let j = 0; j < i; j++) {
        if (s[i] === s[j]) return false;
      }
    }
    return true;
  }
};

/* =====================================================================
   CompMoves — ИИ компьютера (угадывает число игрока)
   ===================================================================== */
class CompMoves {
  constructor(digitQuantity, skill) {
    this.numberSize   = digitQuantity;
    this.skill        = skill;
    this.moveStore    = [];      // [{number, bulls, cows}]
    this.reserveStore = [];
    this.usedIntervals = '';
    this.moveString   = this._shuffleMoveString();
  }

  // --- Подготовка ---

  _shuffleMoveString() {
    let chars;
    do {
      chars = '1234567890'.split('');
      for (let i = 0; i < this.numberSize * 3; i++) {
        const a = Math.floor(Math.random() * 10);
        const b = Math.floor(Math.random() * 10);
        if (a !== b) [chars[a], chars[b]] = [chars[b], chars[a]];
      }
    } while (this._hasLeadingZeroInSegment(chars));
    return chars.join('');
  }

  _hasLeadingZeroInSegment(chars) {
    for (let i = 0; i < 10; i += this.numberSize) {
      if (chars[i] === '0') return true;
    }
    return false;
  }

  // --- Генерация хода ---

  /** Возвращает следующий ход компьютера или null если данные противоречивы. */
  generateMove() {
    const usedDigits = this.numberSize * this.moveStore.length;

    // Начальная фаза: берём числа из предварительно перемешанной строки
    if (this._sumBC() < this.numberSize && 10 - usedDigits >= this.numberSize) {
      return this.moveString.substring(usedDigits, usedDigits + this.numberSize);
    }

    // Основная фаза: ищем число, совместимое со всеми ответами
    this.reserveStore = [...this.moveStore];

    // Уровень мастерства: «забываем» некоторые ходы для снижения точности
    if (this.skill === 2 && this.moveStore.length >= 3)     this._hideWorstMove();
    if (this.skill >= 3 && this.moveStore.length + 1 >= 4)  this._hideBestMove();
    if (this.skill === 4 && this.moveStore.length + 1 >= 5)  this._hideWorstMove();

    let move = null;
    let attempts = 0;
    while (!move && attempts < 1000) {
      attempts++;
      const interval = Math.floor(Math.random() * 9);
      if (this.usedIntervals.includes(String(interval))) continue;

      const base = Math.pow(10, this.numberSize - 1);
      const from = base * (1 + interval);
      const to   = base * (2 + interval);
      const dir  = Math.random() < 0.5 ? 1 : -1;

      move = this._findMove(from, to, dir);
      if (!move) {
        this.usedIntervals += interval;
        if (this.usedIntervals.length === 9) return null; // противоречивые данные
      }
    }

    this.moveStore = [...this.reserveStore];
    return move;
  }

  _findMove(from, to, dir) {
    if (dir < 0) { [from, to] = [to - 1, from - 1]; }
    for (let i = from; i !== to; i += dir) {
      const candidate = String(i);
      if (!BCNumber.isValid(candidate, this.numberSize)) continue;

      // Проверяем совместимость со всеми записанными ответами
      let valid = true;
      for (const m of this.moveStore) {
        if (BCNumber.bools(candidate, m.number) !== m.bulls ||
            BCNumber.cows(candidate, m.number)  !== m.cows) {
          valid = false; break;
        }
      }
      if (!valid) continue;

      // Пропускаем уже использованные ходы
      if (this.reserveStore.some(m => m.number === candidate)) continue;
      return candidate;
    }
    return null;
  }

  _sumBC() {
    return this.moveStore.reduce((s, m) => s + m.bulls + m.cows, 0);
  }

  // --- Стратегии «забывания» (управление сложностью) ---

  _hideBestMove() {
    let bestSum = -1, bestIdx = 0;
    this.moveStore.forEach((m, i) => {
      const sum = m.bulls + m.cows;
      if (sum > bestSum || (sum === bestSum && m.bulls > this.moveStore[bestIdx].bulls)) {
        bestSum = sum; bestIdx = i;
      }
    });
    this.usedIntervals = '';
    this.moveStore.splice(bestIdx, 1);
  }

  _hideWorstMove() {
    let worstSum = Infinity, worstIdx = 0;
    this.moveStore.forEach((m, i) => {
      const sum = m.bulls + m.cows;
      if (sum < worstSum || (sum === worstSum && m.bulls < this.moveStore[worstIdx].bulls)) {
        worstSum = sum; worstIdx = i;
      }
    });
    this.usedIntervals = '';
    this.moveStore.splice(worstIdx, 1);
  }

  // --- Запись ответа ---

  recordAnswer(move, bulls, cows) {
    this.moveStore.push({ number: move, bulls, cows });
  }

  get size() { return this.moveStore.length; }
}

/* =====================================================================
   Состояние игры
   ===================================================================== */
const game = {
  running:    false,
  numberSize: 4,
  gameType:   3,     // 1=комп задумывает, 2=игрок задумывает, 3=оба
  whoFirst:   1,     // 1=игрок, 2=комп
  skill:      2,
  lastMove:   true,

  left: {            // панель игрока (угадывает число компьютера)
    compNumber: null,
    attempts:   [],
    won:        false
  },

  right: {           // панель компьютера (угадывает число игрока)
    compMoves:   null,
    currentMove: null,
    won:         false
  }
};

/* =====================================================================
   Рендеринг панелей
   ===================================================================== */
function renderPanels() {
  const area = document.getElementById('gameArea');
  area.innerHTML = '';
  area.className = 'game-area';

  const showLeft  = game.gameType === 1 || game.gameType === 3;
  const showRight = game.gameType === 2 || game.gameType === 3;

  if (showLeft  && showRight) area.classList.add('both');
  if (!showLeft || !showRight) area.classList.add('single');

  if (showLeft)  area.appendChild(buildLeftPanel());
  if (showRight) area.appendChild(buildRightPanel());
}

function buildLeftPanel() {
  const panel = document.createElement('div');
  panel.id = 'leftPanel';
  panel.className = 'panel';
  panel.innerHTML = `
    <div class="panel-header">
      <div class="panel-header-row">
        <span class="panel-icon">🎯</span>
        <span class="panel-title">Поле игрока</span>
      </div>
      <div class="panel-subtitle">Вы угадываете число компьютера</div>
    </div>
    <div class="table-container">
      <table class="history-table">
        <thead><tr>
          <th>Ход</th>
          <th>🐂 Быки</th>
          <th>🐄 Коровы</th>
        </tr></thead>
        <tbody id="leftTbody"></tbody>
      </table>
    </div>
    <div class="panel-prompt muted" id="leftPrompt">Начните игру</div>
    <div class="input-area">
      <div class="input-row">
        <input type="text" id="leftGuess" class="number-input"
               placeholder="Ваш вариант…" maxlength="8" disabled autocomplete="off">
        <button id="leftBtn" class="btn btn-primary" disabled>Сделать ход</button>
      </div>
      <div class="input-hint">Введите <code>number</code> после 5-го хода, чтобы узнать ответ (засчитается поражение)</div>
    </div>`;
  return panel;
}

function buildRightPanel() {
  const panel = document.createElement('div');
  panel.id = 'rightPanel';
  panel.className = 'panel';
  panel.innerHTML = `
    <div class="panel-header">
      <div class="panel-header-row">
        <span class="panel-icon">🤖</span>
        <span class="panel-title">Поле компьютера</span>
      </div>
      <div class="panel-subtitle">Компьютер угадывает ваше число</div>
    </div>
    <div class="table-container">
      <table class="history-table">
        <thead><tr>
          <th>Ход компьютера</th>
          <th>🐂 Быки</th>
          <th>🐄 Коровы</th>
        </tr></thead>
        <tbody id="rightTbody"></tbody>
      </table>
    </div>
    <div class="panel-prompt muted" id="rightPrompt">Начните игру</div>
    <div class="input-area">
      <div class="input-row">
        <span class="input-label">Быки:</span>
        <input type="number" id="rightBulls" class="answer-input" min="0" max="6" disabled>
        <span class="input-label">Коровы:</span>
        <input type="number" id="rightCows"  class="answer-input" min="0" max="6" disabled>
        <button id="rightBtn" class="btn btn-primary" disabled>Дать ответ</button>
      </div>
    </div>`;
  return panel;
}

/* =====================================================================
   Привязка событий панелей
   ===================================================================== */
function bindPanelListeners() {
  // --- Левая панель (игрок угадывает) ---
  const leftInput = document.getElementById('leftGuess');
  const leftBtn   = document.getElementById('leftBtn');
  if (leftInput && leftBtn) {
    leftInput.addEventListener('keydown', e => { if (e.key === 'Enter') leftBtn.click(); });
    leftBtn.addEventListener('click', onLeftMove);
  }

  // --- Правая панель (компьютер угадывает) ---
  const bullsInput = document.getElementById('rightBulls');
  const cowsInput  = document.getElementById('rightCows');
  const rightBtn   = document.getElementById('rightBtn');
  if (rightBtn) {
    if (bullsInput) bullsInput.addEventListener('keydown', e => { if (e.key === 'Enter') rightBtn.click(); });
    if (cowsInput)  cowsInput.addEventListener('keydown',  e => { if (e.key === 'Enter') rightBtn.click(); });
    rightBtn.addEventListener('click', onRightAnswer);
  }
}

/* =====================================================================
   Обработчики ходов
   ===================================================================== */
function onLeftMove() {
  const input = document.getElementById('leftGuess');
  const val   = (input.value || '').trim();
  if (!val) return;

  // Команда «number» — посмотреть ответ (доступна после 5-го хода)
  if (val.toLowerCase() === 'number') {
    if (game.left.attempts.length >= 4) {
      revealCompNumber();
    } else {
      setPrompt('left', '⚠️ Команда «number» доступна не раньше 5-го хода', 'warn');
    }
    return;
  }

  if (!BCNumber.isValid(val, game.numberSize)) {
    setPrompt('left', `⚠️ Введите корректное ${game.numberSize}-значное число без повторяющихся цифр`, 'error');
    return;
  }

  processLeftMove(val);
}

function processLeftMove(guess) {
  const secret = game.left.compNumber;
  const bulls  = BCNumber.bools(secret, guess);
  const cows   = BCNumber.cows(secret, guess);

  game.left.attempts.push({ guess, bulls, cows });
  addRowToTable('leftTbody', guess, bulls, cows);
  clearInput('leftGuess');

  if (bulls === game.numberSize) {
    // Игрок угадал!
    game.left.won = true;
    const n = game.left.attempts.length;
    const msg = game.left.practicing
      ? `🎉 Нашли число ${secret} за ${n} ${pluralAttempts(n)} (тренировка)`
      : `🎉 Вы разгадали число ${secret} за ${n} ${pluralAttempts(n)}!`;
    setPrompt('left', msg, 'success');
    highlightLastRow('leftTbody', 'win-row');
    lockLeftInput();
    afterLeftWin();
  } else {
    setPrompt('left', `Продолжайте — нужно ${game.numberSize} быков`);
    if (game.gameType === 3) {
      // В режиме «Оба» — передаём ход компьютеру
      if (game.right.won) {
        if (!game.left.practicing) {
          game.left.practicing = true;
          setPrompt('left', `Вы проиграли. Можете продолжить для тренировки`, 'warn');
        }
        // Ввод остаётся открытым — игрок продолжает для тренировки
      } else {
        lockLeftInput();
        doCompTurn();
      }
    }
    // В режиме 1 (только игрок) — просто продолжаем
  }
}

function revealCompNumber() {
  game.left.won = true;
  const secret = game.left.compNumber;
  addRowToTable('leftTbody', secret, game.numberSize, 0);
  setPrompt('left', `👀 Вы подсмотрели число: ${secret} (это засчитывается как поражение)`, 'warn');
  highlightLastRow('leftTbody', 'reveal-row');
  lockLeftInput();
  afterLeftWin();
}

function afterLeftWin() {
  if (game.gameType === 1) {
    endGame();
  } else if (game.gameType === 3) {
    if (game.right.won) {
      endGame();
    } else if (game.lastMove) {
      // Компьютер получает последний шанс
      doCompTurn();
    } else {
      endGame();
    }
  }
}

function onRightAnswer() {
  const bullsVal = document.getElementById('rightBulls').value;
  const cowsVal  = document.getElementById('rightCows').value;
  const bulls    = parseInt(bullsVal);
  const cows     = parseInt(cowsVal);

  if (isNaN(bulls) || isNaN(cows) || bulls < 0 || cows < 0 ||
      bulls + cows > game.numberSize || bulls > game.numberSize) {
    setPrompt('right', '⚠️ Введите корректные значения: быки + коровы ≤ ' + game.numberSize, 'error');
    return;
  }

  processRightAnswer(bulls, cows);
}

function processRightAnswer(bulls, cows) {
  const move = game.right.currentMove;
  game.right.compMoves.recordAnswer(move, bulls, cows);
  addRowToTable('rightTbody', move, bulls, cows);
  clearInput('rightBulls');
  clearInput('rightCows');
  lockRightInput();

  if (bulls === game.numberSize) {
    // Компьютер угадал!
    game.right.won = true;
    const n = game.right.compMoves.size;
    const msg = game.right.practicing
      ? `🤖 Нашёл число за ${n} ${pluralAttempts(n)} (тренировка)`
      : `🤖 Я разгадал ваше число за ${n} ${pluralAttempts(n)}!`;
    setPrompt('right', msg, 'success');
    highlightLastRow('rightTbody', 'win-row');
    afterRightWin();
  } else {
    setPrompt('right', `Жду вашего ответа на следующий ход`);
    if (game.gameType === 3) {
      if (game.left.won) {
        if (!game.right.practicing) {
          game.right.practicing = true;
          setPrompt('right', `🤖 Не угадал — проиграл. Продолжаю для тренировки…`, 'warn');
        }
        setTimeout(() => doCompTurn(), 400);
      } else {
        unlockLeftInput();
        setPrompt('left', `Введите ${game.numberSize}-значное число`);
      }
    } else {
      // Режим 2 (только компьютер угадывает) — компьютер ходит снова
      setTimeout(() => doCompTurn(), 400);
    }
  }
}

function afterRightWin() {
  if (game.gameType === 2) {
    endGame();
  } else if (game.gameType === 3) {
    if (game.left.won) {
      endGame();
    } else if (game.lastMove) {
      // Игрок получает последний шанс
      unlockLeftInput();
      setPrompt('left', `⚡ Ваш последний шанс! Компьютер уже разгадал ваше число.`);
    } else {
      endGame();
    }
  }
}

/* =====================================================================
   Ход компьютера (в правой панели)
   ===================================================================== */
function doCompTurn() {
  const move = game.right.compMoves.generateMove();
  if (!move) {
    setPrompt('right', '❌ Вы ввели противоречивые данные. Компьютер не может продолжить.', 'error');
    lockRightInput();
    endGame();
    return;
  }
  game.right.currentMove = move;
  setPrompt('right', `Мой ход: ${move} — сколько быков и коров?`);
  unlockRightInput();
}

/* =====================================================================
   Управление игрой
   ===================================================================== */
function startGame() {
  game.numberSize = parseInt(document.getElementById('numSize').value);
  game.gameType   = parseInt(document.getElementById('gameType').value);
  game.skill      = parseInt(document.getElementById('skill').value);
  game.whoFirst   = parseInt(document.getElementById('firstMove').value);
  game.lastMove   = document.getElementById('lastMove').checked;
  game.running    = true;

  // Инициализация состояния
  game.left = {
    compNumber: BCNumber.generate(game.numberSize),
    attempts:   [],
    won:        false,
    practicing: false
  };
  game.right = {
    compMoves:   new CompMoves(game.numberSize, game.skill),
    currentMove: null,
    won:         false,
    practicing:  false
  };

  setControlsEnabled(false);
  renderPanels();
  bindPanelListeners();
  updateStatusBar();

  // Запуск по типу игры
  if (game.gameType === 1) {
    // Только игрок угадывает
    unlockLeftInput();
    setPrompt('left', `Введите ${game.numberSize}-значное число`);

  } else if (game.gameType === 2) {
    // Только компьютер угадывает
    setPrompt('right', `Задумайте ${game.numberSize}-значное число, я начну угадывать…`);
    setTimeout(() => doCompTurn(), 600);

  } else {
    // Оба
    if (game.whoFirst === 1) {
      unlockLeftInput();
      setPrompt('left',  `Введите ${game.numberSize}-значное число`);
      setPrompt('right', 'Ожидание вашего хода…', 'muted');
    } else {
      setPrompt('left', 'Ожидание хода компьютера…', 'muted');
      setTimeout(() => doCompTurn(), 400);
    }
  }
}

function breakGame() {
  if (!game.running) return;
  endGame();
  if (game.left.compNumber && !game.left.won) {
    setPrompt('left', `Игра прервана. Число было: ${game.left.compNumber}`, 'warn');
  }
  setStatusBarText('Игра прервана');
}

function endGame() {
  game.running = false;
  lockLeftInput();
  lockRightInput();
  setControlsEnabled(true);
}

/* =====================================================================
   UI: таблицы
   ===================================================================== */
function addRowToTable(tbodyId, number, bulls, cows) {
  const tbody = document.getElementById(tbodyId);
  if (!tbody) return;
  const tr = document.createElement('tr');
  tr.innerHTML = `
    <td>${number}</td>
    <td class="bulls-cell">${bulls}</td>
    <td class="cows-cell">${cows}</td>`;
  tbody.appendChild(tr);
  // Авто-прокрутка
  const container = tbody.closest('.table-container');
  if (container) container.scrollTop = container.scrollHeight;
}

function highlightLastRow(tbodyId, cls) {
  const tbody = document.getElementById(tbodyId);
  if (!tbody) return;
  const rows = tbody.querySelectorAll('tr');
  if (rows.length) rows[rows.length - 1].classList.add(cls);
}

/* =====================================================================
   UI: промпт
   ===================================================================== */
function setPrompt(side, text, type = '') {
  const id = side === 'left' ? 'leftPrompt' : 'rightPrompt';
  const el = document.getElementById(id);
  if (!el) return;
  el.textContent = text;
  el.className = 'panel-prompt ' + type;
}

/* =====================================================================
   UI: управление вводом
   ===================================================================== */
function unlockLeftInput() {
  const input = document.getElementById('leftGuess');
  const btn   = document.getElementById('leftBtn');
  if (input) { input.disabled = false; input.focus(); }
  if (btn)   btn.disabled = false;
}

function lockLeftInput() {
  const input = document.getElementById('leftGuess');
  const btn   = document.getElementById('leftBtn');
  if (input) { input.disabled = true; input.value = ''; }
  if (btn)   btn.disabled = true;
}

function unlockRightInput() {
  const b   = document.getElementById('rightBulls');
  const c   = document.getElementById('rightCows');
  const btn = document.getElementById('rightBtn');
  if (b)   { b.disabled = false; b.value = ''; b.focus(); }
  if (c)   { c.disabled = false; c.value = ''; }
  if (btn) btn.disabled = false;
}

function lockRightInput() {
  const b   = document.getElementById('rightBulls');
  const c   = document.getElementById('rightCows');
  const btn = document.getElementById('rightBtn');
  if (b)   b.disabled = true;
  if (c)   c.disabled = true;
  if (btn) btn.disabled = true;
}

function clearInput(id) {
  const el = document.getElementById(id);
  if (el) el.value = '';
}

/* =====================================================================
   UI: настройки и статус
   ===================================================================== */
function setControlsEnabled(enabled) {
  const ids = ['gameType', 'numSize', 'skill', 'firstMove', 'lastMove'];
  ids.forEach(id => {
    const el = document.getElementById(id);
    if (el) el.disabled = !enabled;
  });
  document.getElementById('btnNewGame').disabled   = !enabled;
  document.getElementById('btnBreakGame').disabled =  enabled;
}

function updateSettingsVisibility() {
  const type = parseInt(document.getElementById('gameType').value);
  document.getElementById('skillGroup').style.display     = type !== 1 ? '' : 'none';
  document.getElementById('firstMoveGroup').style.display = type === 3 ? '' : 'none';
  document.getElementById('lastMoveGroup').style.display  = type === 3 ? '' : 'none';
}

function updateStatusBar() {
  const types  = { 1: 'Вы отгадываете', 2: 'Компьютер отгадывает', 3: 'Оба отгадывают' };
  const skills = { 1: 'Высшее', 2: 'Высокое', 3: 'Среднее', 4: 'Низкое' };
  const firsts = { 1: 'Вы', 2: 'Компьютер' };

  const n = game.numberSize;
  let s = `${types[game.gameType]} | ${n} ${n <= 4 ? 'цифры' : 'цифр'}`;
  if (game.gameType !== 1) s += ` | Мастерство: ${skills[game.skill]}`;
  if (game.gameType === 3) s += ` | Первый ход: ${firsts[game.whoFirst]}`;
  setStatusBarText(s);
}

function setStatusBarText(text) {
  const el = document.getElementById('statusBar');
  if (el) el.textContent = text;
}

/* =====================================================================
   Вспомогательные
   ===================================================================== */
function pluralAttempts(n) {
  const mod10  = n % 10;
  const mod100 = n % 100;
  if (mod10 === 1 && mod100 !== 11)                            return 'попытку';
  if (mod10 >= 2 && mod10 <= 4 && (mod100 < 10 || mod100 >= 20)) return 'попытки';
  return 'попыток';
}

/* =====================================================================
   Инициализация
   ===================================================================== */
document.addEventListener('DOMContentLoaded', () => {

  // Настройки
  document.getElementById('gameType').addEventListener('change', updateSettingsVisibility);
  updateSettingsVisibility();

  // Кнопки управления
  document.getElementById('btnNewGame').addEventListener('click', startGame);
  document.getElementById('btnBreakGame').addEventListener('click', breakGame);

  // Правила
  const helpModal  = document.getElementById('helpModal');
  const openHelp   = () => helpModal.classList.add('active');
  const closeHelp  = () => helpModal.classList.remove('active');

  document.getElementById('btnHelp').addEventListener('click', openHelp);
  document.getElementById('closeHelp').addEventListener('click', closeHelp);
  document.getElementById('closeHelp2').addEventListener('click', closeHelp);
  helpModal.addEventListener('click', e => { if (e.target === helpModal) closeHelp(); });

  // Закрытие по Escape
  document.addEventListener('keydown', e => {
    if (e.key === 'Escape' && helpModal.classList.contains('active')) closeHelp();
  });
});
