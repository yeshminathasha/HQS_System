import { useEffect, useRef, useState } from 'react';

export function usePolling(fetcher, interval = 10000, deps = []) {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [lastUpdated, setLastUpdated] = useState(null);
  const fetcherRef = useRef(fetcher);
  const intervalRef = useRef(null);
  const refreshRef = useRef(() => {});
  fetcherRef.current = fetcher;

  useEffect(() => {
    let active = true;
    const run = async (initial) => {
      if (initial) setLoading(true);
      try {
        const result = await fetcherRef.current();
        if (!active) return;
        setData(result);
        setError(null);
        setLastUpdated(new Date());
      } catch (e) {
        if (!active) return;
        setError(e);
      } finally {
        if (active) setLoading(false);
      }
    };
    const startInterval = () => {
      intervalRef.current = setInterval(() => run(false), interval);
    };
    const stopInterval = () => {
      if (intervalRef.current) {
        clearInterval(intervalRef.current);
        intervalRef.current = null;
      }
    };
    const onVisibilityChange = () => {
      if (document.hidden) {
        stopInterval();
      } else {
        startInterval();
      }
    };

    refreshRef.current = () => run(true);
    run(true);
    startInterval();
    document.addEventListener('visibilitychange', onVisibilityChange);

    return () => {
      active = false;
      stopInterval();
      document.removeEventListener('visibilitychange', onVisibilityChange);
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, deps);

  return { data, loading, error, lastUpdated, refresh: () => refreshRef.current() };
}
