const BASE_URL = import.meta.env.VITE_API_URL  || "http://localhost:8080";

/**
 * Thin fetch wrapper shared by every api/*.js module.
 * Adds the JSON content type, attaches X-User-Id when a userId is passed,
 * and throws a normalized Error (with .status + .body) on non-2xx responses.
 */
export async function request(path, { method = "GET", body, userId, headers = {} } = {}) {
  const finalHeaders = { ...headers };
  if (body !== undefined) finalHeaders["Content-Type"] = "application/json";
  if (userId) finalHeaders["X-User-Id"] = userId;

  const res = await fetch(`${BASE_URL}${path}`, {
    method,
    headers: finalHeaders,
    body: body !== undefined ? JSON.stringify(body) : undefined,
  });

  const text = await res.text();
  const data = text ? safeJsonParse(text) : null;

  if (!res.ok) {
    const message = data?.error || res.statusText || "Request failed";
    const err = new Error(message);
    err.status = res.status;
    err.body = data;
    throw err;
  }
  return data;
}

function safeJsonParse(text) {
  try {
    return JSON.parse(text);
  } catch {
    return text;
  }
}

export function wsUrl(path) {
  const httpBase = BASE_URL.replace(/\/$/, "");
  const wsBase = httpBase.replace(/^http/, "ws");
  return `${wsBase}${path}`;
}

export { BASE_URL };
