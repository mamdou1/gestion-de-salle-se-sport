import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";

interface TableauDeBordProps {
  setIsLoggedIn: (value: boolean) => void;
}

interface CardData {
  titre: string;
  description: string;
  boutonTexte: string;
  onClick: () => void;
}

function TableauDeBord({ setIsLoggedIn }: TableauDeBordProps) {
  const [query, setQuery] = useState<string>("");
  const [showGradient, setShowGradient] = useState<boolean>(false);
  const navigate = useNavigate();

  // 🗂️ Contenu des cartes
  const cardsData: CardData[] = [
    {
      titre: "Bienvenue 👋",
      description: "Gérez vos membres et plannings ici.",
      boutonTexte: "Voir les membres",
      onClick: () => navigate("/membres"),
    },
    {
      titre: "Boutique",
      description: "Consultez vos ventes et produits.",
      boutonTexte: "Voir la boutique",
      onClick: () => console.log("Redirection vers boutique"),
    },
    {
      titre: "Planning",
      description: "Organisez vos coachings et évènements.",
      boutonTexte: "Voir planning",
      onClick: () => navigate("/evenements"), // Mise à jour pour rediriger vers /evenements
    },
  ];

  const handleLogout = () => {
    localStorage.removeItem("token");
    setIsLoggedIn(false);
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setQuery(e.target.value);
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    console.log("Recherche effectuée :", query);
  };

  useEffect(() => {
    const handleScroll = () => {
      const y = window.scrollY;
      const seuil = 300;
      setShowGradient(y > seuil);
    };

    window.addEventListener("scroll", handleScroll);
    return () => window.removeEventListener("scroll", handleScroll);
  }, []);

  return (
    <div className="min-h-screen bg-gradient-to-b from-black via-black to-orange-500 flex flex-col relative">
      <header className="bg-black text-white flex justify-between items-center px-6 py-4 shadow-md">
        <img
          src="./src/assets/logo avec arriere plan supprimer.png"
          alt="logo GYM-PRO"
          width={244}
          height={54}
          className="object-contain"
        />

        {/* Liens de navigation */}
        <nav className="flex space-x-6 font-bold font-inter">
          <a href="#" className="underline text-orange-500 text-xl transition">
            Tableau de bord
          </a>
          <a
            href="/membres"
            onClick={(e) => {
              e.preventDefault();
              navigate("/membres");
            }}
            className="hover:underline hover:text-orange-500 text-xl transition"
          >
            Gestion des membres
          </a>

          <a
            href="/evenements"
            onClick={(e) => {
              e.preventDefault();
              navigate("/evenements");
            }}
            className="text-white hover:underline hover:text-orange-500 text-xl transition"
          >
            Planning
          </a>

          {/* Menu Planning */}
          {/* <div className="relative group">
            <a
              href="/evenements"
                onClick={(e) => {
                  e.preventDefault();
                  navigate("/evenements");
                }}
              className="text-white hover:text-orange-500 text-xl transition"
            >
              Planning
            </a>
            <div className="absolute left-0 mt-1 hidden group-hover:block bg-black border border-orange-600 rounded shadow-lg z-10 text-xs">
              <a
                href="#"
                className="block px-4 py-2 text-white hover:text-orange-400"
              >
                Coaching
              </a>
              <a
                href="/evenements"
                onClick={(e) => {
                  e.preventDefault();
                  navigate("/evenements");
                }}
                className="block px-4 py-2 text-white hover:text-orange-400"
              >
                Évènement
              </a>
            </div>
          </div> */}

          {/* Menu Service */}
          <div className="relative group">
            <a
              href="#"
              className="text-white hover:underline hover:text-orange-500 text-xl transition"
            >
              Service
            </a>
            <div className="absolute left-0 mt-1 hidden group-hover:block bg-black border border-orange-600 rounded shadow-lg z-10 text-xs">
              <a
                href="/abonnements"
                onClick={(e) => {
                  e.preventDefault();
                  navigate("/abonnements");
                }}
                className="block px-4 py-2 text-white hover:text-orange-400"
              >
                Abonnement
              </a>
              <a
                href="#"
                className="block px-4 py-2 text-white hover:text-orange-400"
              >
                Casier
              </a>
              <a
                href="/salles"
                onClick={(e) => {
                  e.preventDefault();
                  navigate("/salles");
                }}
                className="block px-4 py-2 text-white hover:text-orange-400"
              >
                Salle
              </a>
            </div>
          </div>

          {/* Menu Boutique */}
          <div className="relative group">
            <a
              href="#"
              className="text-white hover:underline hover:text-orange-500 text-xl transition"
            >
              Boutique
            </a>
            <div className="absolute left-0 mt-1 hidden group-hover:block bg-black border border-orange-600 rounded shadow-lg z-10 text-xs">
              <a
                href="/produits"
                onClick={(e) => {
                  e.preventDefault();
                  navigate("/produits");
                }}
                className="block px-4 py-2 text-white hover:text-orange-400"
              >
                Produit
              </a>
              <a
                href="/ventes"
                onClick={(e) => {
                  e.preventDefault();
                  navigate("/ventes");
                }}
                className="block px-4 py-2 text-white hover:text-orange-400"
              >
                Vente
              </a>
            </div>
          </div>

          {/* Menu Autres */}
          <div className="relative group">
            <a
              href="#"
              className="text-white hover:underline hover:text-orange-500 text-xl transition"
            >
              Administration
            </a>
            <div className="absolute left-0 mt-1 hidden group-hover:block w-64 bg-black border border-orange-600 rounded shadow-lg z-10 text-xs">
              <a
                href="/staffs"
                onClick={(e) => {
                  e.preventDefault();
                  navigate("/staffs");
                }}
                className="block px-4 py-2 text-white hover:text-orange-400"
              >
                Gestion du staff
              </a>
              <a
                href="#"
                className="block px-4 py-2 text-white hover:text-orange-400"
              >
                Transaction
              </a>
            </div>
          </div>
        </nav>

        {/* Déconnexion */}
        <button
          onClick={handleLogout}
          className="bg-white text-orange-600 font-semibold px-4 py-2 rounded hover:bg-orange-100 transition"
        >
          Déconnexion
        </button>
      </header>

      <main className="flex-1 px-5">
        <div className="m-10">
          <h2 className="text-xl font-semibold text-white mb-2">
            Bienvenue 👋
          </h2>
        </div>

        <div className="flex flex-wrap gap-24 mb-10 px-10">
          <form onSubmit={handleSubmit} className="flex items-center space-x-2">
            <input
              type="text"
              placeholder="Rechercher..."
              value={query}
              onChange={handleChange}
              className="px-4 py-2 rounded-md border border-gray-300 focus:outline-none focus:ring-2 focus:ring-orange-500 text-black w-64"
            />
            <button
              type="submit"
              className="px-4 py-2 bg-orange-600 text-white rounded hover:bg-orange-700 transition"
            >
              Chercher
            </button>
          </form>
          <button className="bg-white text-orange-600 font-semibold px-4 py-2 rounded hover:bg-orange-100 transition">
            Filtre
          </button>
          <button className="bg-white text-orange-600 font-semibold px-4 py-2 rounded hover:bg-orange-100 transition">
            Trier par
          </button>
        </div>

        <div className="flex flex-wrap gap-6 ms-10">
          {cardsData.map((card, index) => (
            <div key={index} className="w-64 bg-white rounded-lg shadow-md p-4">
              <h2 className="text-xl font-semibold text-gray-800 mb-2">
                {card.titre}
              </h2>
              <p className="text-gray-600 mb-2">{card.description}</p>
              <button
                onClick={card.onClick}
                className="px-3 py-2 bg-orange-500 text-white rounded hover:bg-orange-600 transition"
              >
                {card.boutonTexte}
              </button>
            </div>
          ))}
        </div>
      </main>

      {/* Bande orange en bas */}
      <div
        className={`absolute bottom-0 w-full h-40 bg-gradient-to-t from-orange-600 to-transparent transition-opacity duration-700 ${
          showGradient ? "opacity-100" : "opacity-0"
        }`}
      ></div>
    </div>
  );
}

export default TableauDeBord;
