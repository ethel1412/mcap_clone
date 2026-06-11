/*!
* UX4G Accessibility beta v1.15.0 (https://doc.ux4g.gov.in/)
* Copyright 2025 The UX4G Authors(Vipul Agarwal, Ershad Alam, Suman Prasad)
* Copyright 2025 NeGD, MeitY.
* Licensed under MIT. 
* ----- Modified for strict CSP compliance -----
*/

(function () {
    const SETTINGS_KEY = "accessibilitySettings";

    const widgetHTML = `
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>UX4G Accessibility Tool</title>
<link rel="stylesheet" href="https://cdn.ux4g.gov.in/accessibility-beta-v1.15/accessibility-widget.css">

</head>
<body>

<div class="uwaw uw-light-theme gradient-head uwaw-initial paid_widget" id="uw-main" role="dialog" aria-modal="true" aria-labelledby="uw-heading">
    <div class="relative second-panel">
        <h2 id="uw-heading">Accessibility options</h2>
        <button type="button" aria-label="Close main navigation panel" class="uwaw-close" tabindex="2"></button>
    </div>
    <div class="uwaw-body">
        <div class="h-scroll">
            <div class="uwaw-features">
                <div class="uwaw-features__item reset-feature" id="featureItem_sp">
                    <button aria-label="Text To Speech" tabindex="3" id="speak" class="uwaw-features__item__i">
                        <span class="uwaw-features__item__icon">
                            <span class="ux4g-icon icon-speaker" role="img" aria-label="Text to speech icon" aria-hidden="true" aria-pressed="false"></span>
                        </span>
                        <span class="uwaw-features__item__name">Text To Speech</span>
                        <span class="tick-active uwaw-features__item__enabled reset-tick" id="tickIcon_sp"  aria-live="polite" role="status"></span>
                    </button>
                </div>
                <div class="uwaw-features__item reset-feature" id="featureItem">
                    <button aria-label="Bigger Text" tabindex="4" id="btn-s9" class="uwaw-features__item__i">
                        <span class="uwaw-features__item__icon">
                            <span class="ux4g-icon icon-bigger-text" role="img" aria-label="Bigger text icon" aria-hidden="true" aria-pressed="false"></span>
                        </span>
                        <span class="uwaw-features__item__name">Bigger Text</span>
                        <div class="uwaw-features__item__steps reset-steps" id="featureSteps">
                            <span class="step uwaw-features__step"></span>
                            <span class="step uwaw-features__step"></span>
                            <span class="step uwaw-features__step"></span>
                            <span class="step uwaw-features__step"></span>
                        </div>
                        <span class="tick-active uwaw-features__item__enabled reset-tick" id="tickIcon"  aria-live="polite" role="status"></span>
                    </button>
                </div>
                <div class="uwaw-features__item reset-feature" id="featureItem-ts">
                    <button aria-label="Text Spacing" tabindex="5" id="btn-s13" class="uwaw-features__item__i">
                        <span class="uwaw-features__item__icon">
                            <span class="ux4g-icon icon-text-spacing"  role="img" aria-label="Text spacing icon" aria-hidden="true" aria-pressed="false"></span>
                        </span>
                        <span class="uwaw-features__item__name">Text Spacing</span>
                        <div class="uwaw-features__item__steps reset-steps" id="featureSteps-ts">
                            <span class="step uwaw-features__step"></span>
                            <span class="step uwaw-features__step"></span>
                            <span class="step uwaw-features__step"></span>
                        </div>
                        <span class="tick-active uwaw-features__item__enabled reset-tick" id="tickIcon-ts"  aria-live="polite" role="status"></span>
                    </button>
                </div>
                <div class="uwaw-features__item reset-feature" id="featureItem-lh">
                    <button aria-label="Line Height" tabindex="6" id="btn-s12" class="uwaw-features__item__i">
                        <span class="uwaw-features__item__icon">
                            <span class="ux4g-icon icon-line-hight" role="img" aria-label="Line height icon" aria-hidden="true" aria-pressed="false"></span>
                        </span>
                        <span class="uwaw-features__item__name">Line Height</span>
                        <div class="uwaw-features__item__steps reset-steps" id="featureSteps-lh">
                            <span class="step uwaw-features__step"></span>
                            <span class="step uwaw-features__step"></span>
                            <span class="step uwaw-features__step"></span>
                            <span class="step uwaw-features__step"></span>
                        </div>
                        <span class="tick-active uwaw-features__item__enabled reset-tick" id="tickIcon-lh"  aria-live="polite" role="status"></span>
                    </button>
                </div>
                <div class="uwaw-features__item reset-feature" id="featureItem-ht">
                    <button aria-pressed="false" aria-label="Highlight Links" tabindex="7" id="btn-s10" class="uwaw-features__item__i">
                        <span class="uwaw-features__item__icon">
                            <span class="ux4g-icon icon-highlight-links" role="img" aria-label="Highlight links icon" aria-hidden="true"></span>
                        </span>
                        <span class="uwaw-features__item__name">Highlight Links</span>
                        <span class="tick-active uwaw-features__item__enabled reset-tick" id="tickIcon-ht"  aria-live="polite" role="status"></span>
                    </button>
                </div>
                <div class="uwaw-features__item reset-feature" id="featureItem-df">
                    <button aria-label="Dyslexia Friendly Font" aria-pressed="false" tabindex="8" id="btn-df" class="uwaw-features__item__i">
                        <span class="uwaw-features__item__icon">
                            <span class="ux4g-icon icon-dyslexia-font" role="img" aria-label="Dyslexia friendly font icon" aria-hidden="true"></span>
                        </span>
                        <span class="uwaw-features__item__name">Dyslexia Friendly</span>
                        <span class="tick-active uwaw-features__item__enabled reset-tick" id="tickIcon-df"  aria-live="polite" role="status"></span>
                    </button>
                </div>
                <div class="uwaw-features__item reset-feature" id="featureItem-hi">
                    <button aria-label="Hide Images" aria-pressed="false" tabindex="9" id="btn-s11" class="uwaw-features__item__i">
                        <span class="uwaw-features__item__icon">
                            <span class="ux4g-icon icon-hide-images" role="img" aria-label="Hide images icon" aria-hidden="true"></span>
                        </span>
                        <span class="uwaw-features__item__name">Hide Images</span>
                        <span class="tick-active uwaw-features__item__enabled reset-tick" id="tickIcon-hi"  aria-live="polite" role="status"></span>
                    </button>
                </div>
                <div class="uwaw-features__item reset-feature" id="featureItem-Cursor">
                    <button aria-label="Cursor Bigger" aria-pressed="false" tabindex="10" id="btn-cursor" class="uwaw-features__item__i">
                        <span class="uwaw-features__item__icon">
                            <span class="ux4g-icon icon-cursor" role="img" aria-label="Cursor bigger icon" aria-hidden="true"></span>
                        </span>
                        <span class="uwaw-features__item__name">Cursor</span>
                        <span class="tick-active uwaw-features__item__enabled reset-tick" id="tickIcon-cursor"  aria-live="polite" role="status"></span>
                    </button>
                </div>
                <div class="uwaw-features__item reset-feature" id="featureItem-ht-dark">
                    <button aria-label="Light Dark Theme" aria-pressed="false" tabindex="11" id="dark-btn" class="uwaw-features__item__i">
                        <div class="uwaw-features__item__name">
                            <div class="light_dark_icon">
                                <input type="checkbox" class="light_mode uwaw-featugres__item__i" id="checkbox"  aria-label="Toggle light and dark mode" role="switch"/>
                                <label for="checkbox" class="checkbox-label">
                                    <i class="fas fa-moon-stars">
                                        <span class="ux4g-icon icon-moon" role="img" aria-label="Dark mode icon" aria-hidden="true"></span>
                                    </i>
                                    <i class="fas fa-sun">
                                        <span class="ux4g-icon icon-sun" role="img" aria-label="Light mode icon" aria-hidden="true"></span>
                                    </i>
                                    <span class="ball"></span>
                                </label>
                            </div>
                            <span class="uwaw-features__item__name">Light-Dark</span>
                        </div>
                        <span class="tick-active uwaw-features__item__enabled reset-tick" id="tickIcon-ht-dark"  aria-live="polite" role="status"></span>
                    </button>
                </div>
                <div class="uwaw-features__item reset-feature" id="featureItem-ic">
                    <button aria-label="Invert Colors" aria-pressed="false" tabindex="12" id="btn-invert" class="uwaw-features__item__i">
                        <span class="uwaw-features__item__icon">
                            <span class="ux4g-icon icon-invert" role="img" aria-label="Invert colors icon" aria-hidden="true"></span>
                        </span>
                        <span class="uwaw-features__item__name">Invert Colors</span>
                        <span class="tick-active uwaw-features__item__enabled reset-tick" id="tickIcon-ic"  aria-live="polite" role="status"></span>
                    </button>
                </div>
            </div>
        </div>
        </div>
    <div class="reset-panel">
        <div class="copyrights-accessibility">
            <button aria-label="Reset All Settings" tabindex="13" class="btn-reset-all" id="reset-all">
                <div class="reset-icon"></div>
                <div class="reset-btn-text">Reset All Settings</div>
            </button>
            <a tabindex="-1" href="https://www.ux4g.gov.in" target="_blank" class="copyright-text" contenteditable="false">
                <span class="uwaw-features__item__name ux4g-copy ux4g-copyright">Created by</span>
                <img src="https://www.ux4g.gov.in/assets/img/logo/ux4g-logo.svg" alt="UX4G Logo" loading="lazy" />
            </a>
        </div>
    </div>
</div>
<button tabindex="1"  aria-label="Accessibility Options" data-uw-trigger="true" aria-haspopup="dialog" aria-controls="uw-main" aria-expanded="false" id="uw-widget-custom-trigger" class="uw-widget-custom-trigger">
    <img alt="icon" loading="lazy"
        src="data:image/svg+xml,%0A%3Csvg width='32' height='32' viewBox='0 0 32 32' fill='none'
		xmlns='http://www.w3.org/2000/svg'%3E%3Cg clip-path='url(%23clip0_1_1506)'%3E%3Cpath d='M16 7C15.3078 7 14.6311 6.79473 14.0555 6.41015C13.4799 6.02556 13.0313 5.47894 12.7664 4.83939C12.5015 4.19985 12.4322 3.49612 12.5673 2.81719C12.7023 2.13825 13.0356 1.51461 13.5251 1.02513C14.0146 0.535644 14.6383 0.202301 15.3172 0.0672531C15.9961 -0.0677952 16.6999 0.00151652 17.3394 0.266423C17.9789 0.53133 18.5256 0.979934 18.9101 1.55551C19.2947 2.13108 19.5 2.80777 19.5 3.5C19.499 4.42796 19.1299 5.31762 18.4738 5.97378C17.8176 6.62994 16.928 6.99901 16 7Z' fill='white'/%3E%3Cpath d='M27 7.05L26.9719 7.0575L26.9456 7.06563C26.8831 7.08313 26.8206 7.10188 26.7581 7.12125C25.595 7.4625 19.95 9.05375 15.9731 9.05375C12.2775 9.05375 7.14313 7.67875 5.50063 7.21188C5.33716 7.14867 5.17022 7.09483 5.00063 7.05063C3.81313 6.73813 3.00063 7.94438 3.00063 9.04688C3.00063 10.1388 3.98188 10.6588 4.9725 11.0319V11.0494L10.9238 12.9081C11.5319 13.1413 11.6944 13.3794 11.7738 13.5856C12.0319 14.2475 11.8256 15.5581 11.7525 16.0156L11.39 18.8281L9.37813 29.84C9.37188 29.87 9.36625 29.9006 9.36125 29.9319L9.34688 30.0112C9.20188 31.0206 9.94313 32 11.3469 32C12.5719 32 13.1125 31.1544 13.3469 30.0037C13.5813 28.8531 15.0969 20.1556 15.9719 20.1556C16.8469 20.1556 18.6494 30.0037 18.6494 30.0037C18.8838 31.1544 19.4244 32 20.6494 32C22.0569 32 22.7981 31.0162 22.6494 30.0037C22.6363 29.9175 22.6206 29.8325 22.6019 29.75L20.5625 18.8294L20.2006 16.0169C19.9387 14.3788 20.1494 13.8375 20.2206 13.7106C20.2225 13.7076 20.2242 13.7045 20.2256 13.7013C20.2931 13.5763 20.6006 13.2963 21.3181 13.0269L26.8981 11.0763C26.9324 11.0671 26.9662 11.0563 26.9994 11.0438C27.9994 10.6688 28.9994 10.15 28.9994 9.04813C28.9994 7.94625 28.1875 6.73813 27 7.05Z' fill='white'/%3E%3C/g%3E%3Cdefs%3E%3CclipPath id='clip0_1_1506'%3E%3Crect width='32' height='32' fill='white'/%3E%3C/clipPath%3E%3C/defs%3E%3C/svg%3E%0A"
    />
    <span>Accessibility Options</span>
</button>
</body>
</html>`;
    document.body.insertAdjacentHTML('beforeend', widgetHTML);
document.addEventListener("DOMContentLoaded", loadSettings);
document.addEventListener("scroll", function() {
    detectRouteChange()
})
document.getElementById('uw-widget-custom-trigger').addEventListener('click', function() {
    // CSP FIX: Replaced .style with .classList
    document.getElementById('uw-main').classList.add('panel-open');
});

function closeMain() {
    // CSP FIX: Replaced .style with .classList
    document.getElementById('uw-main').classList.remove('panel-open');
}
document.addEventListener('DOMContentLoaded', function() {
    const closeButtons = document.querySelectorAll('.uwaw-close');
    closeButtons.forEach(function(button) {
        button.addEventListener('click', closeMain)
    })
});
let fontSizeCount = 0;
let lineHeightCount = 0;
let textSpacingCount = 0;
let lastPath = window.location.pathname;
let screenReader = !1;
const fontSizeSpans = document.querySelectorAll('#featureSteps span');
const lineHeightSpans = document.querySelectorAll('#featureSteps-lh span');
const textSpacingSpans = document.querySelectorAll('#featureSteps-ts span');
let speechSynthesisInstance = window.speechSynthesis;
let tabPressCount = 0;

function speakText(text) {
    if (!text.trim()) return;
    const utterance = new SpeechSynthesisUtterance(text);
    utterance.lang = "en-US";
    utterance.rate = 1;
    speechSynthesisInstance.cancel();
    speechSynthesisInstance.speak(utterance)
}
document.addEventListener("keydown", (event) => {
    if (event.key === "Tab") {
        tabPressCount++;
        if (tabPressCount === 2) {
            speakText("Press Enter to open accessibility menu.")
        }
    } else if (event.key === "Enter" && tabPressCount === 2) {
        speakText("Opening accessibility menu.");
        tabPressCount = 0
    }
});
document.addEventListener("click", (event) => {
    tabPressCount = 0;
    let element = event.target;
    let clickedText = "";
    if (element.tagName === "IMG") {
        clickedText = element.getAttribute("alt") || element.getAttribute("aria-label") || element.getAttribute("title") || "Clickable image"
    } else {
        clickedText = element.innerText.trim()
    }
    if (clickedText && screenReader) {
        speakText(clickedText)
    }
});
document.addEventListener("mouseup", () => {
    let selectedText = window.getSelection().toString();
    if (selectedText && screenReader) {
        speakText(selectedText)
    }
});
document.addEventListener("mouseover", (event) => {
    let element = event.target;
    let textToSpeak = "";
    if (element.tagName === "IMG" && element.closest("A, BUTTON")) {
        textToSpeak = element.getAttribute("alt") || element.getAttribute("aria-label") || element.getAttribute("title") || "Clickable image"
    } else if (element.tagName === "A" || element.tagName === "BUTTON" || element.tagName === "INPUT" || element.tagName === "TEXTAREA" || element.hasAttribute("role")) {
        textToSpeak = element.innerText || element.getAttribute("aria-label") || element.getAttribute("alt") || element.value || "Interactive element"
    }
    if (textToSpeak && screenReader) {
        speechSynthesisInstance.cancel();
        speakText(textToSpeak)
    }
});

function toggleTextToSpeech() {
    const tickIcon = document.getElementById('tickIcon_sp');
    const button = document.getElementById('featureItem_sp');
    button.classList.toggle('feature-active');
    tickIcon.style.display = tickIcon.style.display === 'inline-flex' ? 'none' : 'inline-flex';
    screenReader = !screenReader;
    saveSettings();
    if (speechSynthesis.speaking) {
        speechSynthesis.cancel()
    }
}
document.addEventListener('DOMContentLoaded', function() {
    const speakButton = document.getElementById('speak');
    if (speakButton) {
        speakButton.addEventListener('click', toggleTextToSpeech)
    }
});

function updateLineHeight() {
    // CSP FIX: Replaced expensive DOM looping with simple body attribute
    if (lineHeightCount === 0) document.body.removeAttribute('data-aw-lh');
    else document.body.setAttribute('data-aw-lh', lineHeightCount);
}

function updateLetterSpacing() {
    // CSP FIX: Replaced expensive DOM looping with simple body attribute
    if (textSpacingCount === 0) document.body.removeAttribute('data-aw-ts');
    else document.body.setAttribute('data-aw-ts', textSpacingCount);
}

function applyTextSettings() {
    // CSP FIX: Replaced expensive DOM looping with simple body attribute
    if (fontSizeCount === 0) document.body.removeAttribute('data-aw-zoom');
    else document.body.setAttribute('data-aw-zoom', fontSizeCount);
}

function adjustFontSize() {
    const button = document.getElementById('featureItem');
    const tickIcon = document.getElementById('tickIcon');
    const fontCheck = document.getElementById('featureSteps');
    button.classList.add('feature-active');
    fontSizeCount = (fontSizeCount + 1) % 5;
    applyTextSettings();
    saveSettings();
    if (fontSizeCount === 0) {
        button.classList.remove('feature-active');
        tickIcon.style.display = 'none';
        fontCheck.classList.remove('featureSteps-visible');
        fontSizeSpans.forEach(span => span.classList.remove('active'))
    } else {
        tickIcon.style.display = 'inline-flex';
        fontCheck.classList.add('featureSteps-visible');
        fontSizeSpans.forEach(span => span.classList.remove('active'));
        fontSizeSpans.forEach((span, index) => {
            if (index <= fontSizeCount - 1) {
                span.classList.add('active')
            }
        })
    }
}
document.addEventListener('DOMContentLoaded', function() {
    const fontSizeBtn = document.getElementById('btn-s9');
    if (fontSizeBtn) {
        fontSizeBtn.addEventListener('click', adjustFontSize)
    }
});

function adjustLineHeight() {
    const button = document.getElementById('featureItem-lh');
    const tickIcon = document.getElementById('tickIcon-lh');
    const lineHeightCheck = document.getElementById('featureSteps-lh');
    button.classList.add('feature-active');
    lineHeightCount = (lineHeightCount + 1) % 5;
    updateLineHeight();
    saveSettings();
    if (lineHeightCount === 0) {
        button.classList.remove('feature-active');
        tickIcon.style.display = 'none';
        lineHeightCheck.classList.remove('featureSteps-visible');
        lineHeightSpans.forEach(span => span.classList.remove('active'))
    } else {
        tickIcon.style.display = 'inline-flex';
        lineHeightSpans.forEach(span => span.classList.remove('active'));
        lineHeightSpans.forEach((span, index) => {
            if (index <= lineHeightCount - 1) {
                span.classList.add('active')
            }
        });
        lineHeightCheck.classList.add('featureSteps-visible')
    }
}
document.addEventListener('DOMContentLoaded', function() {
    const lineHeightBtn = document.getElementById('btn-s12');
    if (lineHeightBtn) {
        lineHeightBtn.addEventListener('click', adjustLineHeight)
    }
});

function adjustTextSpacing() {
    const button = document.getElementById('featureItem-ts');
    button.classList.add('feature-active');
    textSpacingCount = (textSpacingCount + 1) % 4;
    updateLetterSpacing();
    saveSettings();
    const tickIcon = document.getElementById('tickIcon-ts');
    const textSpacingCheck = document.getElementById('featureSteps-ts');
    if (textSpacingCount === 0) {
        button.classList.remove('feature-active');
        tickIcon.style.display = 'none';
        textSpacingCheck.classList.remove('featureSteps-visible');
        textSpacingSpans.forEach(span => span.classList.remove('active'))
    } else {
        tickIcon.style.display = 'inline-flex';
        textSpacingSpans.forEach(span => span.classList.remove('active'));
        textSpacingSpans.forEach((span, index) => {
            if (index <= textSpacingCount - 1) {
                span.classList.add('active')
            }
        });
        textSpacingCheck.classList.add('featureSteps-visible')
    }
}
document.addEventListener('DOMContentLoaded', function() {
    const spacingBtn = document.getElementById('btn-s13');
    if (spacingBtn) {
        spacingBtn.addEventListener('click', adjustTextSpacing)
    }
});

function toggleHighlightLinks() {
    const button = document.getElementById('featureItem-ht');
    const tickIcon = document.getElementById('tickIcon-ht');
    button.classList.toggle('feature-active');
    tickIcon.style.display = tickIcon.style.display === 'inline-flex' ? 'none' : 'inline-flex';
    document.body.classList.toggle('highlight-links');
    let tool = document.querySelector('.uwaw');
    if (tool) {
        tool.classList.remove('highlight-links')
    }
    saveSettings()
}
document.addEventListener('DOMContentLoaded', function() {
    const highlightBtn = document.getElementById('btn-s10');
    if (highlightBtn) {
        highlightBtn.addEventListener('click', toggleHighlightLinks)
    }
});

function toggleDyslexiaMode() {
    const button = document.getElementById('featureItem-df');
    const tickIcon = document.getElementById('tickIcon-df');
    button.classList.toggle('feature-active');
    tickIcon.style.display = tickIcon.style.display === 'inline-flex' ? 'none' : 'inline-flex';
    document.body.classList.toggle('dyslexia-mode');
    saveSettings()
}
document.addEventListener('DOMContentLoaded', function() {
    const dyslexiaBtn = document.getElementById('btn-df');
    if (dyslexiaBtn) {
        dyslexiaBtn.addEventListener('click', toggleDyslexiaMode)
    }
});

function hideImages() {
    const button = document.getElementById('featureItem-hi');
    const tickIcon = document.getElementById('tickIcon-hi');
    button.classList.toggle('feature-active');
    tickIcon.style.display = tickIcon.style.display === 'inline-flex' ? 'none' : 'inline-flex';
    document.body.classList.toggle('hide-images');
    saveSettings()
}
document.addEventListener('DOMContentLoaded', function() {
    const hideImagesBtn = document.getElementById('btn-s11');
    if (hideImagesBtn) {
        hideImagesBtn.addEventListener('click', hideImages)
    }
});

function changeCursor() {
    const button = document.getElementById('featureItem-Cursor');
    const tickIcon = document.getElementById('tickIcon-cursor');
    button.classList.toggle('feature-active');
    tickIcon.style.display = tickIcon.style.display === 'inline-flex' ? 'none' : 'inline-flex';
    document.body.classList.toggle('custom-cursor');
    saveSettings()
}
document.addEventListener('DOMContentLoaded', function() {
    const cursorBtn = document.getElementById('btn-cursor');
    if (cursorBtn) {
        cursorBtn.addEventListener('click', changeCursor)
    }
});

function toggleDarkMode() {
    const button = document.getElementById('featureItem-ht-dark');
    const tickIcon = document.getElementById('tickIcon-ht-dark');
    button.classList.toggle('feature-active');
    tickIcon.style.display = tickIcon.style.display === 'inline-flex' ? 'none' : 'inline-flex';
    document.body.classList.toggle("dark-mode");
    saveSettings()
}
document.addEventListener('DOMContentLoaded', function() {
    const darkModeBtn = document.getElementById('dark-btn');
    if (darkModeBtn) {
        darkModeBtn.addEventListener('click', toggleDarkMode)
    }
});

// on scape key press, close the panel
document.addEventListener('keydown', function (event) {
    if (event.key === 'Escape') {
        const uwMain = document.getElementById('uw-main');
        if (uwMain) {
            uwMain.classList.remove('panel-open');
        }
    }
});

function invertColor() {
    const button = document.getElementById('featureItem-ic');
    const tickIcon = document.getElementById('tickIcon-ic');
    button.classList.toggle('feature-active');
    tickIcon.style.display = tickIcon.style.display === 'inline-flex' ? 'none' : 'inline-flex';
    document.documentElement.classList.toggle("invert-colors");
    saveSettings()
}
document.addEventListener('DOMContentLoaded', function() {
    const invertBtn = document.getElementById('btn-invert');
    if (invertBtn) {
        invertBtn.addEventListener('click', invertColor)
    }
});

function applyADHDFriendlyMode() {
    const allTextNodes = document.body.querySelectorAll('p:not(.uwaw *), h1:not(.uwaw *), h2:not(.uwaw *), h3:not(.uwaw *), h4:not(.uwaw *), h5:not(.uwaw *), h6:not(.uwaw *), span:not(.uwaw *), li:not(.uwaw *), a:not(.uwaw *)');
    allTextNodes.forEach(node => {
        if (node.nodeType === Node.TEXT_NODE && node.textContent.trim().length > 0) {
            let text = node.textContent;
            let updatedText = text.replace(/\b([a-zA-Z0-9]+)\b/g, (match) => {
                if (/<b>|<strong>/.test(match)) {
                    return match
                }
                let word = match;
                if (word.length % 2 === 0) {
                    let halfLength = Math.floor(word.length / 2);
                    // CSP FIX: Replaced inline style block
                    return `<span class="aw-bold-text">${word.substring(0, halfLength)}</span>${word.substring(halfLength)}`
                } else {
                    let halfLength = Math.floor(word.length / 2) + 1;
                    return `<span class="aw-bold-text">${word.substring(0, halfLength)}</span>${word.substring(halfLength)}`
                }
            });
            let tempDiv = document.createElement('div');
            tempDiv.innerHTML = updatedText;
            node.replaceWith(...tempDiv.childNodes)
        } else if (node.nodeType === Node.ELEMENT_NODE) {
            const childNodes = node.childNodes;
            childNodes.forEach(childNode => {
                if (childNode.nodeType === Node.TEXT_NODE && childNode.textContent.trim().length > 0) {
                    let text = childNode.textContent;
                    let updatedText = text.replace(/\b([a-zA-Z0-9]+)\b/g, (match) => {
                        if (/<b>|<strong>/.test(match)) {
                            return match
                        }
                        let word = match;
                        if (word.length % 2 === 0) {
                            let halfLength = Math.floor(word.length / 2);
                            return `<span class="aw-bold-text">${word.substring(0, halfLength)}</span>${word.substring(halfLength)}`
                        } else {
                            let halfLength = Math.floor(word.length / 2) + 1;
                            return `<span class="aw-bold-text">${word.substring(0, halfLength)}</span>${word.substring(halfLength)}`
                        }
                    });
                    let tempDiv = document.createElement('div');
                    tempDiv.innerHTML = updatedText;
                    childNode.replaceWith(...tempDiv.childNodes)
                }
            })
        }
    })
}
window.toggleADHDFriendlyMode = function() {
    const button = document.getElementById('featureItem-adhd');
    button.classList.toggle('feature-active');
    const tickIcon = document.getElementById('tickIcon-adhd');
    tickIcon.style.display = tickIcon.style.display === 'inline-flex' ? 'none' : 'inline-flex';
    document.body.classList.toggle('adhd-friendly');
    if (document.body.classList.contains('adhd-friendly')) {
        saveSettings();
        applyADHDFriendlyMode()
    } else {
        const allTextNodes = document.body.querySelectorAll('p:not(.uwaw *), h1:not(.uwaw *), h2:not(.uwaw *), h3:not(.uwaw *), h4:not(.uwaw *), h5:not(.uwaw *), h6:not(.uwaw *), span:not(.uwaw *), li:not(.uwaw *), a:not(.uwaw *)');
        allTextNodes.forEach(node => {
            // CSP FIX: Replaced inline style regex strip
            node.innerHTML = node.innerHTML.replace(/<span class="aw-bold-text">(.*?)<\/span>/g, '$1')
        });
        saveSettings()
    }
};

function resetSettings() {
    localStorage.removeItem(SETTINGS_KEY);
    speechSynthesisInstance.cancel();
    const allTextNodes = document.body.querySelectorAll('p:not(.uwaw *), h1:not(.uwaw *), h2:not(.uwaw *), h3:not(.uwaw *), h4:not(.uwaw *), h5:not(.uwaw *), h6:not(.uwaw *), span:not(.uwaw *), li:not(.uwaw *), a:not(.uwaw *)');
    allTextNodes.forEach(node => {
        node.innerHTML = node.innerHTML.replace(/<span class="aw-bold-text">(.*?)<\/span>/g, '$1')
    });

    // CSP FIX: Replaced explicit styling loops with removing CSS data-attributes
    document.body.removeAttribute('data-aw-ts');
    document.body.removeAttribute('data-aw-lh');
    document.body.removeAttribute('data-aw-zoom');

    document.body.classList.remove("dark-mode", "adhd-friendly", "custom-cursor", "dyslexia-mode", "highlight-links", "hide-images");
    document.documentElement.classList.remove("invert-colors");
    fontSizeCount = 0;
    lineHeightCount = 0;
    textSpacingCount = 0;
    screenReader = !1;
    saveSettings();
    loadSettings();
    const checkboxes = document.querySelectorAll('.uwaw input[type="checkbox"]');
    checkboxes.forEach(checkbox => checkbox.checked = !1);
    document.querySelectorAll('.font-size-visible, .line-height-visible, .text-spacing-visible').forEach(el => el.classList.remove('span-visible'))
}

document.addEventListener('DOMContentLoaded', function() {
    const resetBtn = document.getElementById('reset-all');
    if (resetBtn) {
        resetBtn.addEventListener('click', resetSettings)
    }
});

function saveSettings() {
    localStorage.setItem(SETTINGS_KEY, JSON.stringify({
        screenReader: screenReader,
        fontSizeCount: fontSizeCount - 1,
        lineHeightCount: lineHeightCount - 1,
        textSpacingCount: textSpacingCount - 1,
        highlightLinks: document.body.classList.contains('highlight-links'),
        dyslexiaMode: document.body.classList.contains('dyslexia-mode'),
        hideImages: document.body.classList.contains('hide-images'),
        darkMode: document.body.classList.contains('dark-mode'),
        cursorChanged: document.body.classList.contains('custom-cursor'),
        invert: document.documentElement.classList.contains('invert-colors'),
        adhdFriendly: document.body.classList.contains('adhd-friendly')
    }))
}

function updateWidgetToggles(settings) {
    const speakOn = document.getElementById('tickIcon_sp');
    if (speakOn) {
        speakOn.style.display = settings.screenReader ? 'inline-flex' : 'none'
    }
    const highlightToggle = document.getElementById('tickIcon-ht');
    if (highlightToggle) {
        highlightToggle.style.display = settings.highlightLinks ? 'inline-flex' : 'none'
    }
    const darkModeToggle = document.getElementById('tickIcon-ht-dark');
    if (darkModeToggle) {
        darkModeToggle.style.display = settings.darkMode ? 'inline-flex' : 'none'
    }
    const invertToggle = document.getElementById('tickIcon-ic');
    if (invertToggle) {
        invertToggle.style.display = settings.invert ? 'inline-flex' : 'none'
    }
    const dyslexiaToggle = document.getElementById('tickIcon-df');
    if (dyslexiaToggle) {
        dyslexiaToggle.style.display = settings.dyslexiaMode ? 'inline-flex' : 'none'
    }
    const adhdToggle = document.getElementById('tickIcon-adhd');
    if (adhdToggle) {
        adhdToggle.style.display = settings.adhdFriendly ? 'inline-flex' : 'none'
    }
    const hideImagesToggle = document.getElementById('tickIcon-hi');
    if (hideImagesToggle) {
        hideImagesToggle.style.display = settings.hideImages ? 'inline-flex' : 'none'
    }
    const cursorToggle = document.getElementById('tickIcon-cursor');
    if (cursorToggle) {
        cursorToggle.style.display = settings.cursorChanged ? 'inline-flex' : 'none'
    }

    // CSP FIX: Functions only called once rather than in loops
    applyTextSettings();
    updateLineHeight();
    updateLetterSpacing();
}

function loadSettings() {
    const settings = JSON.parse(localStorage.getItem(SETTINGS_KEY));
    if (settings) {
        fontSizeCount = settings.fontSizeCount || 0;
        lineHeightCount = settings.lineHeightCount || 0;
        textSpacingCount = settings.textSpacingCount || 0;
        if (settings.screenReader) {
            screenReader = settings.screenReader;
            const button = document.getElementById('featureItem_sp');
            button.classList.add('feature-active')
        } else {
            const button = document.getElementById('featureItem_sp');
            button.classList.remove('feature-active')
        }
        if (settings.highlightLinks) {
            document.body.classList.add('highlight-links');
            const button = document.getElementById('featureItem-ht');
            button.classList.add('feature-active')
        } else {
            const button = document.getElementById('featureItem-ht');
            button.classList.remove('feature-active')
        }
        if (settings.dyslexiaMode) {
            document.body.classList.add('dyslexia-mode');
            const button = document.getElementById('featureItem-df');
            button.classList.add('feature-active')
        } else {
            const button = document.getElementById('featureItem-df');
            button.classList.remove('feature-active')
        }
        if (settings.hideImages) {
            document.body.classList.add('hide-images');
            const button = document.getElementById('featureItem-hi');
            button.classList.add('feature-active')
        } else {
            const button = document.getElementById('featureItem-hi');
            button.classList.remove('feature-active')
        }
        if (settings.darkMode) {
            document.body.classList.add('dark-mode');
            const button = document.getElementById('featureItem-ht-dark');
            button.classList.add('feature-active')
        } else {
            const button = document.getElementById('featureItem-ht-dark');
            button.classList.remove('feature-active')
        }
        if (settings.cursorChanged) {
            document.body.classList.add('custom-cursor');
            const button = document.getElementById('featureItem-Cursor');
            button.classList.add('feature-active')
        } else {
            const button = document.getElementById('featureItem-Cursor');
            button.classList.remove('feature-active')
        }
        if (settings.invert) {
            document.documentElement.classList.toggle("invert-colors");
            const button = document.getElementById('featureItem-ic');
            button.classList.add('feature-active')
        } else {
            const button = document.getElementById('featureItem-ic');
            button.classList.remove('feature-active')
        }
        if (settings.adhdFriendly) {
            document.body.classList.add('adhd-friendly');
            applyADHDFriendlyMode()
        }

        updateWidgetToggles(settings)
    }
}

function detectRouteChange() {
    const settings = JSON.parse(localStorage.getItem(SETTINGS_KEY));
    setInterval(() => {
        let currentPath = window.location.pathname;
        if (currentPath !== lastPath) {
            speechSynthesisInstance.cancel();
            lastPath = currentPath;
            if (settings.adhdFriendly) {
                document.body.classList.add('adhd-friendly');
                applyADHDFriendlyMode()
            }
            updateLineHeight();
            updateLetterSpacing();
        }
    }, 1000)
}
})()