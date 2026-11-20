# Compte Rendu — TP Android : Gestion de Contacts (Version 4)

**Module :** Développement Mobile • Android • Kotlin  
**Sujet :** Persistance SQLite, Préférences utilisateur, Fragments, Google Maps  

---

## 1. Introduction et Objectifs
La version 4 de l'application de gestion de contacts introduit deux évolutions majeures :
1. **La persistance permanente des données** via une base de données **SQLite** locale (`contacts.db`), remplaçant la collection en mémoire de la version 3.
2. **Un écran de préférences utilisateur** (`SharedPreferences` & `PreferenceFragmentCompat`) permettant de personnaliser dynamiquement la taille de police, la couleur du texte et l'affichage d'un bouton d'appel direct dans la liste.

---

## 2. Architecture et Persistance SQLite

### 2.1. Schéma de la Base de Données (`contacts`)
La table `contacts` comporte les colonnes suivantes :
- `_id` : `INTEGER PRIMARY KEY AUTOINCREMENT` (ID unique du contact)
- `nom` : `TEXT NOT NULL`
- `prenom` : `TEXT NOT NULL`
- `telephone` : `TEXT NOT NULL`
- `email` : `TEXT`
- `photo_uri` : `TEXT`
- `latitude` : `REAL`
- `longitude` : `REAL`

### 2.2. Composants DAO et Helper
- **`ContactDbHelper`** : Hérite de `SQLiteOpenHelper` (implémenté en Singleton avec `applicationContext`). Crée la table et insère 5 contacts de démonstration (dont 2 sans coordonnées GPS) lors de la création initiale (`onCreate`).
- **`ContactDao`** : Centralise toutes les requêtes SQL. Utilise exclusivement des requêtes paramétrées avec des arguments `?` (`selectionArgs`) pour éviter toute injection SQL. Les requêtes trient les résultats par nom et prénom de manière insensible à la casse (`COLLATE NOCASE`).
- **Asynchronisme** : Tous les accès à la base de données sont exécutés hors du thread principal en utilisant `viewModelScope` et `withContext(Dispatchers.IO)`.

---

## 3. Préférences Utilisateur
- **`ParametresFragment`** : Étend `PreferenceFragmentCompat` et charge la description depuis `res/xml/preferences.xml`.
- **`PreferencesManager`** : Centralise la lecture et la conversion des préférences (`taille_police`, `couleur_preferee`, `appel_depuis_liste`).
- **Application immédiate** : Lors du retour sur `ListeContactsFragment` (`onResume`), les préférences sont relues et appliquées instantanément à l'adaptateur de la `RecyclerView`.

---

## 4. Réponses aux Questions de Compréhension (Version 4)

### 1. Quand les méthodes `onCreate` et `onUpgrade` d’un `SQLiteOpenHelper` sont-elles appelées ? Que se passe-t-il si l’on modifie `onCreate` sans changer la version ?
- `onCreate` est appelée une seule fois, lorsque le fichier de base de données n’existe pas encore sur l’appareil.
- `onUpgrade` est appelée lorsque l’application est lancée avec un numéro `DATABASE_VERSION` supérieur à celui présent sur l'appareil.
- Si l’on modifie le code de `onCreate` sans incrémenter `DATABASE_VERSION` ni désinstaller l'application, la base existe déjà et `onCreate` ne sera **pas** exécutée. Les modifications de schéma ne seront donc pas appliquées.

### 2. Pourquoi utiliser des arguments `?` plutôt que concaténer les valeurs dans une requête SQL ? Donnez un exemple d’entrée qui casse une requête concaténée.
- Les arguments `?` (`selectionArgs`) permettent de paramétrer les requêtes de façon sécurisée : SQLite traite la valeur comme une simple donnée littérale et non comme du code SQL exécutable, éliminant les risques d'**injections SQL**.
- *Exemple d'entrée destructrice :* Si un utilisateur saisit un nom tel que `O'Neil`, une concaténation brute donnerait `WHERE nom = 'O'Neil'`, ce qui ferme prématurément la chaîne SQL par l'apostrophe et provoque une erreur de syntaxe ou une faille de sécurité.

### 3. Que renvoient `insert()`, `update()` et `delete()` ? Comment exploitez-vous ces valeurs dans l’interface ?
- `insert()` renvoie l’identifiant (`_id`) de la nouvelle ligne insérée (ou `-1` en cas d'échec).
- `update()` et `delete()` renvoient le **nombre de lignes modifiées ou supprimées**.
- Dans l’interface, ces retours permettent de valider le succès de l'opération avant d'afficher un message de confirmation (`Toast`) et de revenir à la liste.

### 4. Pourquoi l’accès à la base ne doit-il pas se faire sur le thread principal ? Comment l’avez-vous évité ?
- Les accès disque/BDD peuvent être lents et bloquer le thread principal (UI thread), provoquant des micro-saccades, des janks, ou pire, une erreur `ANR` (Application Not Responding).
- Nous l'avons évité en utilisant les **coroutines Kotlin** (`viewModelScope.launch` associé à `withContext(Dispatchers.IO)`).

### 5. Pourquoi le helper doit-il être unique et construit avec `applicationContext` ?
- **Unique (Singleton) :** Évite d'ouvrir de multiples connexions simultanées vers la même base SQLite, ce qui consommerait des ressources et pourrait provoquer des conflits d’écriture.
- **`applicationContext` :** Empêche les fuites de mémoire (Memory Leaks) en évitant de stocker une référence vers le contexte d’une Activité ou d'un Fragment dont le cycle de vie est plus court.

### 6. SharedPreferences ou SQLite : selon quels critères choisir l’un ou l’autre ? Justifiez le choix fait pour les contacts et pour les préférences.
- **SharedPreferences :** Conçu pour des données de configuration simples sous forme de paires clé-valeur de petite taille (ex: préférences utilisateur, états booléens).
- **SQLite :** Conçu pour des ensembles de données structurées, relationnelles, volumineuses nécessitant des recherches, des tris et des index (ex: les contacts).
- C'est pourquoi les contacts sont stockés en SQLite et les réglages d'affichage dans les SharedPreferences.

### 7. Comment les préférences modifiées sont-elles appliquées à la liste sans redémarrer l’application ?
- Dans la méthode `onResume()` de `ListeContactsFragment`, l’application recharge les préférences via le `PreferencesManager` et met à jour l’adaptateur de la `RecyclerView` en temps réel.

### 8. Quelle différence existe-t-il entre `ACTION_DIAL` et `ACTION_CALL`, notamment en matière de permissions ?
- `ACTION_DIAL` ouvre le composeur téléphonique prérempli avec le numéro : l’utilisateur garde le contrôle et décide d’appuyer sur le bouton d'appel (**aucune permission requise**).
- `ACTION_CALL` déclenche directement l'appel sans passer par le composeur : elle nécessite une permission sensible (`CALL_PHONE`) déclarée dans le manifeste et demandée dynamiquement à l’utilisateur.

### 9. Que deviennent la base et les préférences lorsque l’application est désinstallée ? Et lorsque l’utilisateur efface les données de l’application ?
- Dans les deux cas (désinstallation ou effacement des données via les paramètres système), le fichier SQLite et le fichier de SharedPreferences sont **totalement supprimés** du stockage de l'appareil par le système Android.

---

## 5. Tests et Validation
L'application a été validée avec succès sur tous les scénarios :
- Premier lancement (insertion des 5 contacts démo) et relance ultérieure (pas de doublons).
- CRUD complet (Ajout, Modification, Suppression avec `AlertDialog`).
- Saisie de caractères spéciaux (apostrophes) et de coordonnées GPS valides ou invalides.
- Application immédiate des préférences (taille, couleur, bouton d'appel).
- Persistance complète après fermeture de l'application et redémarrage.
