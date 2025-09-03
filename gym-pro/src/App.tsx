import { useState } from "react";
import {
  BrowserRouter as Router,
  Routes,
  Route,
  Navigate,
} from "react-router-dom";
import ConnexionForm from "./components/ConnexionForms";
import InscriptionForm from "./components/InscriptionFrorms";
import TableauDeBord from "./components/TableauDeBords";
import GestionMembre from "./components/GestionMembre";
import Evennement from "./components/Evennement";
import GestionProduit from "./components/GestionProduit";
import GestionVente from "./components/GestionVente";
import GestionAbonnement from "./components/GestionAbonnement";
import GestionStaff from "./components/GestionStaff";
import GestionSalle from "./components/GestionSalle";
import GestionCasier from "./components/GestionCasier";
import GestionGymAbonnement from "./components/GestionGymAbonnement";

function App() {
  const [isLoggedIn, setIsLoggedIn] = useState(false);

  return (
    <Router>
      <Routes>
        <Route
          path="/connexion"
          element={
            !isLoggedIn ? (
              <ConnexionForm setIsLoggedIn={setIsLoggedIn} />
            ) : (
              <Navigate to="/" replace />
            )
          }
        />
        <Route
          path="/inscription"
          element={
            !isLoggedIn ? (
              <InscriptionForm setIsLoggedIn={setIsLoggedIn} /> // Affiche le formulaire d'inscription
            ) : (
              <Navigate to="/" replace />
            )
          }
        />
        <Route
          path="/"
          element={
            isLoggedIn ? (
              <TableauDeBord setIsLoggedIn={setIsLoggedIn} />
            ) : (
              <Navigate to="/connexion" replace />
            )
          }
        />
        <Route
          path="/membres"
          element={
            isLoggedIn ? (
              <GestionMembre setIsLoggedIn={setIsLoggedIn} />
            ) : (
              <Navigate to="/connexion" replace />
            )
          }
        />
        <Route
          path="/evenements"
          element={
            isLoggedIn ? (
              <Evennement setIsLoggedIn={setIsLoggedIn} />
            ) : (
              <Navigate to="/connexion" replace />
            )
          }
        />
        <Route
          path="/produits"
          element={
            isLoggedIn ? (
              <GestionProduit setIsLoggedIn={setIsLoggedIn} />
            ) : (
              <Navigate to="/connexion" replace />
            )
          }
        />
        <Route
          path="/ventes"
          element={
            isLoggedIn ? (
              <GestionVente setIsLoggedIn={setIsLoggedIn} />
            ) : (
              <Navigate to="/connexion" replace />
            )
          }
        />
        <Route
          path="/abonnements"
          element={
            isLoggedIn ? (
              <GestionAbonnement setIsLoggedIn={setIsLoggedIn} />
            ) : (
              <Navigate to="/connexion" replace />
            )
          }
        />
        <Route
          path="/staffs"
          element={
            isLoggedIn ? (
              <GestionStaff setIsLoggedIn={setIsLoggedIn} />
            ) : (
              <Navigate to="/connexion" replace />
            )
          }
        />
        <Route
          path="/salles"
          element={
            isLoggedIn ? (
              <GestionSalle setIsLoggedIn={setIsLoggedIn} />
            ) : (
              <Navigate to="/connexion" replace />
            )
          }
        />
        <Route
          path="/casiers"
          element={
            isLoggedIn ? (
              <GestionCasier setIsLoggedIn={setIsLoggedIn} />
            ) : (
              <Navigate to="/connexion" replace />
            )
          }
        />
        <Route
          path="/abonnements-gyms"
          element={
            isLoggedIn ? (
              <GestionGymAbonnement setIsLoggedIn={setIsLoggedIn} />
            ) : (
              <Navigate to="/connexion" replace />
            )
          }
        />
      </Routes>
    </Router>
  );
}

export default App;
