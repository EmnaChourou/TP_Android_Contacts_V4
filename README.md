# TP Android — Gestion de Contacts (Version 4)

Application Android développée en **Kotlin** sous **Android Studio**, implémentant la persistance des données via **SQLite**, un écran de **préférences utilisateur**, des fragments dynamiques, une `RecyclerView` filtrable et l'intégration du **SDK Google Maps**.

## Nouvelles Fonctionnalités de la Version 4
1. **Persistance SQLite (`contacts.db`)** :
   - Remplacement de la collection en mémoire par une base SQLite native (`SQLiteOpenHelper`, `SQLiteDatabase`, `ContentValues`, `Cursor`).
   - Insertion automatique de 5 contacts de démonstration au premier lancement.
   - Requêtes DAO sécurisées et paramétrées (`selectionArgs` avec `?`).
   - Traitements asynchrones via Kotlin Coroutines (`viewModelScope`, `Dispatchers.IO`).
   - Sauvegarde des photos dans le stockage interne (`filesDir`) pour garantir leur persistance après redémarrage.
2. **Préférences Utilisateur (`SharedPreferences` & `PreferenceFragmentCompat`)** :
   - Écran de paramètres (`ParametresFragment`) pour régler :
     - La **Taille de police** (Petite 14sp, Moyenne 16sp, Grande 20sp).
     - La **Couleur du texte** (Noir, Bleu, Vert, Rouge, Violet).
     - L'activation du **Bouton d'appel direct** depuis chaque ligne de la liste (`SwitchPreferenceCompat`).
   - Application immédiate des réglages au retour sur la liste des contacts sans redémarrage.
3. **CRUD Complet** :
   - **Create** : Bouton flottant `+` pour ajouter un contact.
   - **Read** : Liste et détail chargés depuis SQLite.
   - **Update** : Modification et sauvegarde en base.
   - **Delete** : Suppression avec boîte de dialogue de confirmation (`AlertDialog`).

## Configuration de la clé Google Maps API
Dans le fichier `local.properties` à la racine du projet :
```properties
MAPS_API_KEY=AIzaSyAJSBeCw829Hbxrc0fpcUgxXnwtHzai2Xo
```

