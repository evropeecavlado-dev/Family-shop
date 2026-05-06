# 🛒 FamilyShop — Семеен пазарски списък

Android приложение с реалновременна синхронизация чрез Firebase.

## Технологии
- **Kotlin** + **Android**
- **Firebase Auth** (Google Sign-In)
- **Firebase Firestore** (реалновременна база)
- **Firebase Functions** (автоматично изтриване)
- **Hilt** (Dependency Injection)
- **Flow** (реактивен UI)

---

## Настройка на Firebase

### 1. Създай Firebase проект
1. Отиди на [console.firebase.google.com](https://console.firebase.google.com)
2. "Add project" → дай му име → създай
3. Добави Android апп с package name: `com.familyshop`
4. Свали `google-services.json` и го постави в `app/`

### 2. Активирай Authentication
- Firebase Console → Authentication → Sign-in method
- Включи **Google**

### 3. Активирай Firestore
- Firebase Console → Firestore Database → Create database
- Избери **production mode**
- Публикувай правилата от `firestore.rules`

### 4. Web Client ID
- Firebase Console → Project Settings → General → Your apps → Web app
- Копирай **Web client ID** и го постави в `res/values/strings.xml`

### 5. Deploy Cloud Functions (за автоматично изтриване)
```bash
npm install -g firebase-tools
firebase login
firebase init functions
cd functions && npm install
firebase deploy --only functions
```

---

## Структура на проекта

```
app/src/main/java/com/familyshop/
├── data/
│   ├── model/ShoppingItem.kt       # Модел на данните
│   └── repository/ShoppingRepository.kt  # Firebase операции
├── di/AppModule.kt                 # Hilt зависимости
├── ui/
│   ├── login/LoginActivity.kt      # Google влизане
│   └── list/
│       ├── ShoppingListActivity.kt # Главен екран
│       ├── ShoppingListViewModel.kt
│       └── ShoppingItemAdapter.kt  # RecyclerView адаптер
└── FamilyShopApp.kt

functions/
└── index.js    # Cloud Function за изтриване след 7 дни

firestore.rules # Правила за сигурност
```

---

## Структура на данните в Firestore

```
shopping_items/
  {documentId}/
    name: "Мляко"
    status: "PENDING" | "DONE"
    createdAt: Timestamp
    completedAt: Timestamp | null
    deleteAfter: Timestamp | null   ← автоматично изтриване
```

---

## Функционалности

- ✅ Влизане с Google акаунт
- ✅ Добавяне на продукти
- ✅ Отмятане (PENDING ↔ DONE)
- ✅ Реалновременна синхронизация (Firestore snapshot listener)
- ✅ Дата на добавяне и дата на изпълнение
- ✅ Автоматично изтриване след 7 дни (Cloud Function)
- ✅ Свайп за ръчно изтриване

---

## Следващи стъпки (по желание)

- [ ] Push нотификации при добавен продукт
- [ ] Категории на продуктите
- [ ] Количество (напр. „2 л", „500 г")
- [ ] Тъмен режим
- [ ] Поддръжка на множество семейства/списъци
