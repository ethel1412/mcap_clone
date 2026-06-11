function getPublicKey() {
	const csrfToken = document.querySelector('meta[name="_csrf"]').content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

    axios.defaults.headers.common[csrfHeader] = csrfToken;
  
  return axios.post("/key/get-publickey",null,{
    headers: {
      "X-Requested-With": "XMLHttpRequest"
    }, 
    withCredentials: true
  })
  .then(response => response.data)
  .catch(error => {
    alert(
      "Error: " + error.message +
      "\nStatus: " + (error.response?.status || "N/A") +
      "\nResponse: " + (error.response?.data || "No response")
    );
    throw error;
  });
}

function pemToArrayBuffer(pem) {
  const b64 = pem
    .replace("-----BEGIN PUBLIC KEY-----", "")
    .replace("-----END PUBLIC KEY-----", "")
    .replace(/\s/g, "");
    
  const binary = atob(b64);
  const buffer = new Uint8Array(binary.length);

  for (let i = 0; i < binary.length; i++) {
    buffer[i] = binary.charCodeAt(i);
  }

  return buffer;
}

async function importKey(pem) {
  return await crypto.subtle.importKey(
    "spki",
    pemToArrayBuffer(pem),
    {
      name: "RSA-OAEP",
      hash: "SHA-256"
    },
    false,
    ["encrypt"]
  );
}

async function encryptText(key, text) {
  const encoded = new TextEncoder().encode(text);

  const encrypted = await crypto.subtle.encrypt(
    { name: "RSA-OAEP" },
    key,
    encoded
  );

  return btoa(String.fromCharCode(...new Uint8Array(encrypted)));
}


function getCaptcha() {
  axios.post("/captcha/get-captcha",null,{
    headers: {
      "X-Requested-With": "XMLHttpRequest"
    },
    withCredentials: true
  })
  .then(function (response) {

    const container = document.getElementById("captchaImage");
    if (!container) return;

    container.replaceChildren();

    const img = document.createElement("img");
    img.alt = "CAPTCHA";
    img.src = "data:image/jpeg;base64," + response.data;

    container.appendChild(img);
  })
  .catch(function () {
    alert("Captcha load failed");
  });
}