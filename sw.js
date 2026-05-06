// Service Worker за Семеен списък
// Версия 2 — с Push нотификации

importScripts('https://www.gstatic.com/firebasejs/10.12.0/firebase-app-compat.js');
importScripts('https://www.gstatic.com/firebasejs/10.12.0/firebase-messaging-compat.js');

var firebaseConfig = {
  apiKey:            "AIzaSyBh650O1UrKL6FpbgDjZf35EPZD-CIha3c",
  authDomain:        "family-shop-900e4.firebaseapp.com",
  projectId:         "family-shop-900e4",
  storageBucket:     "family-shop-900e4.firebasestorage.app",
  messagingSenderId: "1026599977960",
  appId:             "1:1026599977960:web:598d794361f258317d9a5d"
};

firebase.initializeApp(firebaseConfig);
var messaging = firebase.messaging();

// Показваме нотификация когато приложението е на заден план
messaging.onBackgroundMessage(function(payload){
  var n = payload.notification || {};
  var title = n.title || 'Семеен списък';
  var body  = n.body  || '';

  self.registration.showNotification(title, {
    body:  body,
    icon:  '/Family-shop/icon-192.png',
    badge: '/Family-shop/icon-192.png',
    vibrate: [200, 100, 200],
    data: { url: '/Family-shop/' }
  });
});

// Клик върху нотификацията отваря приложението
self.addEventListener('notificationclick', function(event){
  event.notification.close();
  event.waitUntil(
    clients.openWindow(event.notification.data.url || '/Family-shop/')
  );
});

// Кешираме основните файлове за офлайн работа
var CACHE = 'family-shop-v2';
var FILES = ['/Family-shop/', '/Family-shop/index.html'];

self.addEventListener('install', function(event){
  event.waitUntil(
    caches.open(CACHE).then(function(cache){
      return cache.addAll(FILES);
    })
  );
  self.skipWaiting();
});

self.addEventListener('activate', function(event){
  event.waitUntil(
    caches.keys().then(function(keys){
      return Promise.all(
        keys.filter(function(k){ return k !== CACHE; })
            .map(function(k){ return caches.delete(k); })
      );
    })
  );
  self.clients.claim();
});

self.addEventListener('fetch', function(event){
  // Само за GET заявки към нашите файлове
  if(event.request.method !== 'GET') return;
  event.respondWith(
    fetch(event.request).catch(function(){
      return caches.match(event.request);
    })
  );
});
