import { useEffect, useRef, useState } from "react";
import { trackingSocketUrl, getTracking } from "../api/tracking";

/**
 * Subscribes to WS /v1/orders/{orderId}/track for live driver location/status
 * updates. Falls back to polling GET /v1/delivery/{orderId}/tracking every 5s
 * if the socket can't connect (e.g. no driver assigned yet, or WS blocked).
 */
export function useOrderTracking(orderId) {
  const [tracking, setTracking] = useState(null);
  const [connected, setConnected] = useState(false);
  const pollRef = useRef(null);

  useEffect(() => {
    if (!orderId) return undefined;

    let socket;
    let cancelled = false;

    const startPolling = () => {
      if (pollRef.current) return;
      pollRef.current = setInterval(async () => {
        try {
          const res = await getTracking(orderId);
          if (!cancelled && res?.found) setTracking(res.lastKnownUpdate);
        } catch {
          // not tracked yet — keep polling quietly
        }
      }, 5000);
    };

    try {
      socket = new WebSocket(trackingSocketUrl(orderId));
      socket.onopen = () => {
        if (cancelled) return;
        setConnected(true);
        if (pollRef.current) {
          clearInterval(pollRef.current);
          pollRef.current = null;
        }
      };
      socket.onmessage = (event) => {
        if (cancelled) return;
        try {
          setTracking(JSON.parse(event.data));
        } catch {
          // ignore malformed frame
        }
      };
      socket.onclose = () => {
        if (cancelled) return;
        setConnected(false);
        startPolling();
      };
      socket.onerror = () => {
        socket.close();
      };
    } catch {
      startPolling();
    }

    // Always do one immediate REST fetch so the page has data before the
    // socket connects (or if it never does).
    getTracking(orderId)
      .then((res) => {
        if (!cancelled && res?.found) setTracking(res.lastKnownUpdate);
      })
      .catch(() => {});

    return () => {
      cancelled = true;
      socket?.close();
      if (pollRef.current) clearInterval(pollRef.current);
    };
  }, [orderId]);

  return { tracking, connected };
}
