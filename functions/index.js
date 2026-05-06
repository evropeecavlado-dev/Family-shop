const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();
const db = admin.firestore();

/**
 * Изпълнява се веднъж дневно.
 * Изтрива всички продукти с deleteAfter < сега.
 */
exports.deleteExpiredItems = functions
  .region("europe-west1")
  .pubsub.schedule("every 24 hours")
  .onRun(async (context) => {
    const now = admin.firestore.Timestamp.now();

    const snapshot = await db
      .collection("shopping_items")
      .where("deleteAfter", "<=", now)
      .get();

    if (snapshot.empty) {
      console.log("Няма изтекли продукти.");
      return null;
    }

    const batch = db.batch();
    snapshot.docs.forEach((doc) => batch.delete(doc.ref));
    await batch.commit();

    console.log(`Изтрити ${snapshot.size} продукта.`);
    return null;
  });
