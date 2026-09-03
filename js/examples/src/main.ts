import '@nepali-date-picker/web-component';
import type { NepaliDatePickerDialog } from '@nepali-date-picker/web-component';
import { convertAdToBs, getTodayAd, getTodayBs } from '@nepali-date-picker/core';

type Lang = 'en' | 'ne';

const $ = <T extends Element>(selector: string): T => {
  const el = document.querySelector<T>(selector);
  if (!el) throw new Error(`missing element: ${selector}`);
  return el;
};

/* Global language + theme controls apply to every picker on the page. */
function applyLanguage(lang: Lang): void {
  for (const el of document.querySelectorAll('.sync')) el.setAttribute('language', lang);
}

$('#global-language').addEventListener('change', (e) => {
  applyLanguage((e.target as HTMLSelectElement).value as Lang);
});

$('#global-theme').addEventListener('change', (e) => {
  document.body.dataset.theme = (e.target as HTMLSelectElement).value;
});

/* Event log: every picker's change / cancel / invalid event bubbles here. */
const log = $('#log');
function record(name: string, detail: unknown, source: string): void {
  const entry = document.createElement('div');
  entry.className = 'log-entry';
  const body = detail === undefined ? '' : `<pre>${JSON.stringify(detail, null, 2)}</pre>`;
  entry.innerHTML = `<span class="name">${name}</span> from <code>${source}</code>${body}`;
  log.prepend(entry);
  while (log.children.length > 12) log.lastElementChild?.remove();
}

for (const type of ['change', 'cancel', 'invalid'] as const) {
  document.addEventListener(type, (e) => {
    const target = e.target as HTMLElement;
    if (!target.tagName?.toLowerCase().startsWith('nepali-')) return;
    record(type, (e as CustomEvent).detail, `<${target.tagName.toLowerCase()}>`);
  });
}

/* Dialog open buttons. */
$('#open-dialog').addEventListener('click', () => ($('#dialog-el') as NepaliDatePickerDialog).show());
$('#open-dialog-fs').addEventListener('click', () => ($('#dialog-fs') as NepaliDatePickerDialog).show());

/* Framework code tabs. */
for (const tab of document.querySelectorAll<HTMLButtonElement>('.tab')) {
  tab.addEventListener('click', () => {
    const name = tab.dataset.tab;
    for (const t of document.querySelectorAll<HTMLButtonElement>('.tab')) {
      t.setAttribute('aria-selected', String(t === tab));
    }
    for (const panel of document.querySelectorAll<HTMLElement>('.tabpanel')) {
      if (panel.dataset.tab === name) panel.setAttribute('data-active', '');
      else panel.removeAttribute('data-active');
    }
  });
}

/* Engine section. */
const todayBs = getTodayBs();
const todayAd = getTodayAd();
$('#today-bs').textContent = `${todayBs.year}-${pad(todayBs.month)}-${pad(todayBs.dayOfMonth)}`;
$('#today-ad').textContent = `${todayAd.year}-${pad(todayAd.month)}-${pad(todayAd.dayOfMonth)}`;

const adInput = $<HTMLInputElement>('#ad-input');
adInput.value = `${todayAd.year}-${pad(todayAd.month)}-${pad(todayAd.dayOfMonth)}`;
const updateConversion = (): void => {
  const [y, m, d] = adInput.value.split('-').map(Number);
  if (!y || !m || !d) return;
  try {
    const bs = convertAdToBs(y, m, d);
    $('#ad-to-bs').textContent = `${bs.year}-${pad(bs.month)}-${pad(bs.dayOfMonth)}`;
  } catch {
    $('#ad-to-bs').textContent = 'out of range';
  }
};
adInput.addEventListener('change', updateConversion);
updateConversion();

/* Form submit demo. */
$('#demo-form').addEventListener('submit', (e) => {
  e.preventDefault();
  const field = $('#demo-form nepali-date-field') as HTMLElement & { value: string };
  record('submit', { value: field.value }, '<form>');
});

function pad(n: number): string {
  return n < 10 ? `0${n}` : String(n);
}
