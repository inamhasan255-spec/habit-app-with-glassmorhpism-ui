const CACHE_NAME = 'focuscube-offline-cache-v1';
const ASSETS_TO_CACHE = [
    'https://appassets.androidplatform.net/assets/tesseract_ocr.html',
    'https://appassets.androidplatform.net/assets/whisper_transcriber.html',
    'https://cdn.jsdelivr.net/npm/tesseract.js@5/dist/tesseract.min.js',
    'https://cdn.jsdelivr.net/npm/@xenova/transformers@2.17.2'
];

self.addEventListener('install', event => {
    event.waitUntil(
        caches.open(CACHE_NAME).then(cache => {
            return cache.addAll(ASSETS_TO_CACHE);
        }).then(() => self.skipWaiting())
    );
});

self.addEventListener('activate', event => {
    event.waitUntil(self.clients.claim());
});

self.addEventListener('fetch', event => {
    // Serve from cache first, then fall back to network
    event.respondWith(
        caches.match(event.request).then(cachedResponse => {
            if (cachedResponse) {
                return cachedResponse;
            }
            return fetch(event.request).then(response => {
                // Cache successful responses for future offline requests (including CDN model files)
                if (response && (response.status === 200 || response.status === 0)) {
                    const responseClone = response.clone();
                    caches.open(CACHE_NAME).then(cache => {
                        cache.put(event.request, responseClone);
                    });
                }
                return response;
            }).catch(() => {
                // Return offline message or local resource if network fails
            });
        })
    );
});
