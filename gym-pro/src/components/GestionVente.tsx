import React, { useState, useEffect } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";

interface Vente {
  id: number;
  dateVente: string;
  montantTotal: number;
  modeDePaiement: string;
  membre: { id: number; nom: string; prenom: string } | null;
  staff: { id: number; nom: string; prenom: string };

  lignes: LigneVente[];
}

interface LigneVente {
  id: number;
  produit: { id: number; nom: string; prixUnitaire: number };
  quantite: number;
  prixTotal: number;
}

interface VenteManuelDTO {
  acheteurId?: number;
  nomAcheteur?: string;
  prenomAcheteur?: string;
  telephoneAcheteur?: string;
  genre?: string;
  modeDePaiement: string;
  produitIds: number[];
  quantites: number[];
}

interface Produit {
  id: number;
  nom: string;
  prixUnitaire: number;
}

interface Membre {
  id: number;
  nom: string;
  prenom: string;
}

interface TableauDeBordProps {
  setIsLoggedIn: (value: boolean) => void;
}

function GestionVente({ setIsLoggedIn }: TableauDeBordProps) {
  const [ventes, setVentes] = useState<Vente[]>([]);
  const [selectedVente, setSelectedVente] = useState<Vente | null>(null);
  const [addingVente, setAddingVente] = useState<boolean>(false);
  const [formData, setFormData] = useState<VenteManuelDTO>({
    modeDePaiement: "CASH",
    produitIds: [],
    quantites: [],
  });
  const [loadingProduits, setLoadingProduits] = useState<boolean>(false);
  const [produits, setProduits] = useState<Produit[]>([]);
  const [membres, setMembres] = useState<Membre[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [detailLoading, setDetailLoading] = useState<boolean>(false);
  const [adding, setAdding] = useState<boolean>(false);
  const [error, setError] = useState<string>("");
  const [success, setSuccess] = useState<string>("");
  const [currentPage, setCurrentPage] = useState<number>(1);
  const ventesPerPage = 10;
  const [stats, setStats] = useState({
    journalier: { nombre: 0, montant: 0 },
    hebdomadaire: { nombre: 0, montant: 0 },
    mensuel: { nombre: 0, montant: 0 },
    annuel: { nombre: 0, montant: 0 },
  });
  const navigate = useNavigate();
  const token = localStorage.getItem("token");

  const [query, setQuery] = useState<string>("");
  const [filterProduit, setFilterProduit] = useState<string>("");
  const [membreSearch, setMembreSearch] = useState<string>("");
  const [filteredMembres, setFilteredMembres] = useState<Membre[]>([]);

  const handleLogout = () => {
    localStorage.removeItem("token");
    setIsLoggedIn(false);
  };

  // Récupérer les ventes
  const fetchVentes = async () => {
    try {
      const response = await axios.get<Vente[]>(
        "http://localhost:8080/api/ventes/lister",
        {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        }
      );
      setVentes(response.data);
      console.log("Ventes récupérées:", response.data);
    } catch (err: any) {
      setError("Erreur lors de la récupération des ventes.");
      console.error(
        "Erreur détaillée (ventes):",
        err.response?.data || err.message
      );
    }
  };

  // Récupérer les détails d'une vente
  const fetchVenteDetails = async (id: number) => {
    setDetailLoading(true);
    try {
      const response = await axios.get<Vente>(
        `http://localhost:8080/api/ventes/${id}`,
        {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        }
      );
      setSelectedVente(response.data);
      console.log("Détails de la vente:", response.data);
    } catch (err: any) {
      setError("Erreur lors de la récupération des détails de la vente.");
      console.error(
        "Erreur détaillée (détails vente):",
        err.response?.data || err.message
      );
    } finally {
      setDetailLoading(false);
    }
  };

  // Récupérer les produits
  const fetchProduits = async () => {
    setLoadingProduits(true); // Démarre le chargement
    try {
      const response = await axios.get<Produit[]>(
        "http://localhost:8080/api/produits/lister",
        {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        }
      );
      setProduits(response.data);
      console.log("Produits récupérés:", response.data);
    } catch (err: any) {
      setError("Erreur lors de la récupération des produits.");
      console.error(
        "Erreur détaillée (produits):",
        err.response?.data || err.message
      );
      setProduits([]); // Initialise avec un tableau vide en cas d'erreur
    } finally {
      setLoadingProduits(false); // Termine le chargement
    }
  };

  // Récupérer les membres
  const fetchMembres = async () => {
    try {
      const response = await axios.get<Membre[]>(
        "http://localhost:8080/api/users/membre",
        {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        }
      );
      setMembres(response.data);
      console.log("Membres récupérés:", response.data);
    } catch (err: any) {
      setError(
        "Erreur lors de la récupération des membres. Vous pouvez toujours enregistrer une vente pour un non-membre."
      );
      setMembres([]); // Liste vide pour permettre l'ajout de non-membres
      console.error(
        "Erreur détaillée (membres):",
        err.response?.data || err.message
      );
    }
  };

  // Récupérer les statistiques
  const fetchStats = async () => {
    const endpoints = [
      { key: "journalier.nombre", url: "/statistiques/nombre/journalier" },
      { key: "journalier.montant", url: "/statistiques/montant/journalier" },
      { key: "hebdomadaire.nombre", url: "/statistiques/nombre/hebdomadaire" },
      {
        key: "hebdomadaire.montant",
        url: "/statistiques/montant/hebdomadaire",
      },
      { key: "mensuel.nombre", url: "/statistiques/nombre/mensuel" },
      { key: "mensuel.montant", url: "/statistiques/montant/mensuel" },
      { key: "annuel.nombre", url: "/statistiques/nombre/annuel" },
      { key: "annuel.montant", url: "/statistiques/montant/annuel" },
    ];

    try {
      const results = await Promise.all(
        endpoints.map(async ({ key, url }) => {
          try {
            const response = await axios.get<number>(
              `http://localhost:8080/api/ventes${url}`,
              {
                headers: { Authorization: `Bearer ${token}` },
              }
            );
            console.log(`Données reçues pour ${url}:`, response.data);
            return { key, value: Number(response.data) || 0 };
          } catch (err: any) {
            console.error(
              `Erreur pour ${url}:`,
              err.response?.data || err.message
            );
            return { key, value: 0 };
          }
        })
      );

      const newStats = {
        journalier: { nombre: 0, montant: 0 },
        hebdomadaire: { nombre: 0, montant: 0 },
        mensuel: { nombre: 0, montant: 0 },
        annuel: { nombre: 0, montant: 0 },
      };

      results.forEach(({ key, value }) => {
        const [periode, type] = key.split(".");
        newStats[periode][type] = value;
      });

      setStats(newStats);
      console.log("Statistiques mises à jour:", newStats);
    } catch (err: any) {
      setError(
        "Erreur lors de la récupération des statistiques. Certaines données peuvent être indisponibles."
      );
      console.error(
        "Erreur globale (stats):",
        err.response?.data || err.message
      );
    }
  };

  // Préparer le formulaire d'ajout
  const prepareAddForm = () => {
    setAddingVente(true);
    setFormData({
      modeDePaiement: "CASH",
      produitIds: [],
      quantites: [],
    });
  };

  // Ajouter une vente
  const handleAddVente = async (e: React.FormEvent) => {
    e.preventDefault();
    if (formData.produitIds.length === 0 || formData.quantites.length === 0) {
      setError("Veuillez sélectionner au moins un produit et une quantité.");
      return;
    }
    // if (
    //   !formData.acheteurId &&
    //   (!formData.nomAcheteur ||
    //     !formData.prenomAcheteur ||
    //     !formData.telephoneAcheteur)
    // ) {
    //   setError("Veuillez fournir les informations de l'acheteur non-membre.");
    //   return;
    // }
    setAdding(true);
    try {
      await axios.post(
        "http://localhost:8080/api/ventes/enregistrer-manuelle",
        formData,
        {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        }
      );
      setSuccess("Vente enregistrée avec succès !");
      setAddingVente(false);
      fetchVentes();
      fetchStats();
      setTimeout(() => setSuccess(""), 3000);
    } catch (err: any) {
      setError(
        `Erreur lors de l'enregistrement : ${
          err.response?.data?.message || err.message
        }`
      );
      console.error(
        "Erreur détaillée (ajout vente):",
        err.response?.data || err.message
      );
    } finally {
      setAdding(false);
    }
  };

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
  ) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]:
        name === "acheteurId" ? (value ? parseInt(value) : undefined) : value,
    }));
  };

  const handleProduitChange = (
    index: number,
    field: "produitId" | "quantite",
    value: string
  ) => {
    setFormData((prev) => {
      const produitIds = [...prev.produitIds];
      const quantites = [...prev.quantites];
      if (field === "produitId") {
        produitIds[index] = parseInt(value);
      } else {
        quantites[index] = parseInt(value);
      }
      return { ...prev, produitIds, quantites };
    });
  };

  const addProduitField = () => {
    setFormData((prev) => ({
      ...prev,
      produitIds: [...prev.produitIds, 0],
      quantites: [...prev.quantites, 1],
    }));
  };

  const removeProduitField = (index: number) => {
    setFormData((prev) => {
      const produitIds = prev.produitIds.filter((_, i) => i !== index);
      const quantites = prev.quantites.filter((_, i) => i !== index);
      return { ...prev, produitIds, quantites };
    });
  };

  const closeDetails = () => {
    setSelectedVente(null);
  };

  const closeAddForm = () => {
    setAddingVente(false);
  };

  const indexOfLastVente = currentPage * ventesPerPage;
  const indexOfFirstVente = indexOfLastVente - ventesPerPage;
  const currentVentes = ventes.slice(indexOfFirstVente, indexOfLastVente);
  const totalPages = Math.ceil(ventes.length / ventesPerPage);

  const handlePreviousPage = () => {
    if (currentPage > 1) {
      setCurrentPage(currentPage - 1);
    }
  };

  const handleNextPage = () => {
    if (currentPage < totalPages) {
      setCurrentPage(currentPage + 1);
    }
  };

  useEffect(() => {
    if (!token) {
      setError(
        "Aucun token d'authentification trouvé. Veuillez vous reconnecter."
      );
      navigate("/login");
      return;
    }
    const fetchData = async () => {
      await Promise.all([
        fetchVentes(),
        fetchProduits(),
        fetchMembres(),
        fetchStats(),
      ]);
      setLoading(false);
    };
    fetchData();
  }, []);

  const handleSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setQuery(e.target.value);
  };

  const handleFilterChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    setFilterProduit(e.target.value);
  };

  const filteredVentes = ventes.filter((vente) => {
    const matchesQuery =
      !query ||
      vente.lignes.some((ligne) =>
        ligne.produit.nom.toLowerCase().includes(query.toLowerCase())
      ) ||
      (vente.membre &&
        `${vente.membre.nom} ${vente.membre.prenom}`
          .toLowerCase()
          .includes(query.toLowerCase())) ||
      vente.modeDePaiement.toLowerCase().includes(query.toLowerCase());

    const matchesFilter =
      !filterProduit ||
      vente.lignes.some((ligne) => ligne.produit.nom === filterProduit);

    return matchesQuery && matchesFilter;
  });

  if (loading) {
    return (
      <div className="min-h-screen bg-black flex items-center justify-center">
        <div className="text-white text-xl">Chargement des ventes...</div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-b from-black via-black to-orange-500 flex flex-col">
      <header className="fixed w-full top-0 bg-black text-white flex justify-between items-center px-6 py-4 shadow-md">
        <img
          src="./src/assets/logo avec arriere plan supprimer.png"
          alt="logo GYM-PRO"
          width={244}
          height={54}
          className="object-contain"
        />
        <nav className="flex space-x-6 font-bold font-inter">
          <button
            onClick={() => navigate("/")}
            className="hover:underline hover:text-orange-500 text-xl transition cursor-pointer text-white bg-transparent border-none"
          >
            Tableau de bord
          </button>
          <div className="relative group">
            <button className="text-white hover:text-orange-500 text-xl transition cursor-pointer bg-transparent border-none">
              Administration
            </button>
            <div className="absolute left-0 mt-1 hidden group-hover:block w-64 bg-black border border-orange-600 rounded shadow-lg z-10 text-xs">
              <button className="block w-full text-left px-4 py-2 text-white hover:text-orange-400 cursor-pointer">
                Gestion des membres
              </button>
              <button className="block w-full text-left px-4 py-2 text-white hover:text-orange-400 cursor-pointer">
                Gestion des produits
              </button>
              <button className="block w-full text-left px-4 py-2 text-white hover:text-orange-400 cursor-pointer bg-orange-600">
                Gestion des ventes
              </button>
              <button className="block w-full text-left px-4 py-2 text-white hover:text-orange-400 cursor-pointer">
                Gestion du staff
              </button>
            </div>
          </div>
        </nav>
        <button
          onClick={handleLogout}
          className="bg-white text-orange-600 font-semibold px-4 py-2 rounded hover:bg-orange-100 transition"
        >
          Déconnexion
        </button>
      </header>

      <main className="flex-1 pt-24 p-6">
        <div className="flex justify-between items-center mb-6">
          <div>
            <h1 className="text-3xl font-bold text-white mb-2">
              Gestion des Ventes
            </h1>
            <p className="text-gray-400">
              {ventes.length} vente{ventes.length !== 1 ? "s" : ""} trouvée
              {ventes.length !== 1 ? "s" : ""} dans votre salle de sport
            </p>
          </div>
          <button
            onClick={prepareAddForm}
            className="bg-orange-500 text-white font-bold px-6 py-2 rounded-lg hover:bg-white hover:text-orange-500 transition-colors"
          >
            + Enregistrer une vente
          </button>
        </div>

        {/* Nouveau champ de recherche et filtre */}
        <div className="flex flex-wrap gap-24 mb-10 pt-2">
          <form
            onSubmit={(e) => e.preventDefault()}
            className="flex items-center space-x-2"
          >
            <input
              type="text"
              placeholder="Rechercher..."
              value={query}
              onChange={handleSearchChange}
              className="px-4 py-2 rounded-md border border-gray-300 focus:outline-none focus:ring-2 focus:ring-orange-500 text-black w-64"
            />
          </form>
          <div className="flex items-center space-x-2">
            <select
              value={filterProduit}
              onChange={handleFilterChange}
              className="px-4 py-2 rounded-md border border-gray-300 focus:outline-none focus:ring-2 focus:ring-orange-500 text-black w-64"
            >
              <option value="">Tous les produits</option>
              {produits.map((produit) => (
                <option key={produit.id} value={produit.nom}>
                  {produit.nom}
                </option>
              ))}
            </select>
          </div>
        </div>

        {/* Statistiques */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
          <div className="bg-white rounded-lg shadow-lg p-4">
            <h3 className="text-lg font-semibold">Journalier</h3>
            <p>Nombres: {stats.journalier.nombre}</p>
            <p>Montant: {stats.journalier.montant} FCFA</p>
          </div>
          <div className="bg-white rounded-lg shadow-lg p-4">
            <h3 className="text-lg font-semibold">Hebdomadaire</h3>
            <p>Nombres: {stats.hebdomadaire.nombre}</p>
            <p>Montant: {stats.hebdomadaire.montant} FCFA</p>
          </div>
          <div className="bg-white rounded-lg shadow-lg p-4">
            <h3 className="text-lg font-semibold">Mensuel</h3>
            <p>Nombres: {stats.mensuel.nombre}</p>
            <p>Montant: {stats.mensuel.montant} FCFA</p>
          </div>
          <div className="bg-white rounded-lg shadow-lg p-4">
            <h3 className="text-lg font-semibold">Annuel</h3>
            <p>Nombres: {stats.annuel.nombre}</p>
            <p>Montant: {stats.annuel.montant} FCFA</p>
          </div>
        </div>

        {error && (
          <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
            {error}
          </div>
        )}
        {success && (
          <div className="bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded mb-4">
            {success}
          </div>
        )}

        {/* Tableau des ventes */}
        <div className="bg-white rounded-lg shadow-lg overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-orange-500 text-white">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider">
                    Date
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider">
                    Acheteur
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider">
                    Montant Total
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider">
                    Mode de Paiement
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {filteredVentes.map((vente) => (
                  <tr
                    key={vente.id}
                    className="hover:bg-gray-300 cursor-pointer transition-colors odd:bg-gray-100 even:bg-gray-200"
                    onClick={() => fetchVenteDetails(vente.id)}
                  >
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm text-gray-900">
                        {new Date(vente.dateVente).toLocaleDateString()}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm text-gray-900">
                        {vente.membre
                          ? `${vente.membre.nom} ${vente.membre.prenom}`
                          : "Non-nombre"}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm text-gray-900">
                        {vente.montantTotal} FCFA
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm text-gray-900">
                        {vente.modeDePaiement}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        {filteredVentes.length === 0 && (
          <div className="text-center py-8 text-gray-500 bg-white rounded-lg mt-4">
            Aucun abonnement trouvé avec les filtres appliqués.
          </div>
        )}

        {/* Pagination */}
        {ventes.length > 0 && (
          <div className="mt-6 flex justify-center items-center space-x-4">
            <button
              onClick={handlePreviousPage}
              disabled={currentPage === 1}
              className="bg-orange-500 text-white px-4 py-2 rounded-lg hover:bg-orange-600 transition-colors disabled:opacity-50"
            >
              Précédent
            </button>
            <span className="text-white">
              Page {currentPage} sur {totalPages}
            </span>
            <button
              onClick={handleNextPage}
              disabled={currentPage === totalPages}
              className="bg-orange-500 text-white px-4 py-2 rounded-lg hover:bg-orange-600 transition-colors disabled:opacity-50"
            >
              Suivant
            </button>
          </div>
        )}

        {ventes.length === 0 && (
          <div className="text-center py-8 text-gray-500 bg-white rounded-lg mt-4">
            Aucune vente trouvée dans la base de données.
          </div>
        )}

        {/* Bouton d'actualisation */}
        <div className="mt-6 flex justify-center">
          <button
            onClick={() => {
              fetchVentes();
              fetchStats();
            }}
            className="bg-orange-500 text-white px-6 py-2 rounded-lg hover:bg-white hover:text-orange-500 transition-colors"
          >
            Actualiser la liste
          </button>
        </div>

        {/* Modal des détails de la vente */}
        {selectedVente && (
          <div
            className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
            onClick={(e) => {
              const modal = e.currentTarget.querySelector(".bg-white");
              if (modal && !modal.contains(e.target as Node)) {
                closeDetails();
              }
            }}
          >
            <div className="bg-white rounded-lg p-6 w-full max-w-2xl max-h-90vh overflow-y-auto">
              <div className="flex justify-between items-center mb-4">
                <h2 className="text-2xl font-bold">Détails de la Vente</h2>
                <button
                  onClick={closeDetails}
                  className="text-gray-500 hover:text-gray-700 text-2xl"
                >
                  ×
                </button>
              </div>

              {detailLoading ? (
                <div className="text-center py-8">
                  Chargement des détails...
                </div>
              ) : (
                <>
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-6">
                    <div>
                      <h3 className="text-orange-500 font-semibold text-lg mb-3">
                        Informations de la vente
                      </h3>
                      <div className="space-y-2">
                        <p>
                          <strong>Date:</strong>{" "}
                          {new Date(
                            selectedVente.dateVente
                          ).toLocaleDateString()}
                        </p>
                        <p>
                          <strong>Acheteur:</strong>{" "}
                          {selectedVente.membre
                            ? `${selectedVente.membre.nom} ${selectedVente.membre.prenom}`
                            : "Non-membre"}
                        </p>
                        <p>
                          <strong>Montant Total:</strong>{" "}
                          {selectedVente.montantTotal} FCFA
                        </p>
                        <p>
                          <strong>Mode de Paiement:</strong>{" "}
                          {selectedVente.modeDePaiement}
                        </p>
                        <p>
                          <strong>Vente éffectuer par:</strong>{" "}
                          {selectedVente.staff.nom} {selectedVente.staff.prenom}
                        </p>
                      </div>
                    </div>
                    <div>
                      <h3 className="text-orange-500 font-semibold text-lg mb-3">
                        Produits
                      </h3>
                      <ul className="space-y-2">
                        {selectedVente.lignes.map((ligne) => (
                          <li key={ligne.id}>
                            <strong>{ligne.produit.nom}</strong>:{" "}
                            {ligne.quantite} x {ligne.produit.prixUnitaire} FCFA
                            = {ligne.prixTotal} FCFA
                          </li>
                        ))}
                      </ul>
                    </div>
                  </div>
                  <div className="flex justify-end">
                    <button
                      onClick={closeDetails}
                      className="bg-black text-orange-500 px-4 py-2 rounded hover:bg-gray-600 transition-colors"
                    >
                      Fermer
                    </button>
                  </div>
                </>
              )}
            </div>
          </div>
        )}

        {/* Modal d'ajout de vente */}
        {addingVente && (
          <div
            className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
            onClick={(e) => {
              const modal = e.currentTarget.querySelector(".bg-white");
              if (modal && !modal.contains(e.target as Node)) {
                closeAddForm();
              }
            }}
          >
            <div className="bg-white rounded-lg p-6 w-full max-w-2xl max-h-90vh overflow-y-auto">
              <div className="flex justify-between items-center mb-4">
                <h2 className="text-2xl font-bold">
                  Enregistrer une Nouvelle Vente
                </h2>
                <button
                  onClick={closeAddForm}
                  className="text-gray-500 hover:text-gray-700 text-2xl"
                >
                  ×
                </button>
              </div>

              {loadingProduits ? (
                <div className="text-center py-4">
                  Chargement des produits...
                </div>
              ) : (
                <form onSubmit={handleAddVente} className="space-y-4">
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                      <label className="block text-gray-700">
                        Acheteur (membre existant)
                      </label>
                      <input
                        type="text"
                        value={membreSearch || ""}
                        onChange={(e) => {
                          const searchTerm = e.target.value;
                          setMembreSearch(searchTerm);
                          const filteredMembres = membres.filter((membre) =>
                            `${membre.nom} ${membre.prenom}`
                              .toLowerCase()
                              .includes(searchTerm.toLowerCase())
                          );
                          setFilteredMembres(filteredMembres);
                        }}
                        placeholder="Rechercher un membre..."
                        className="w-full p-2 border rounded mb-2"
                      />
                      {filteredMembres.length > 0 && (
                        <ul className="border rounded max-h-40 overflow-y-auto">
                          {filteredMembres.map((membre) => (
                            <li
                              key={membre.id}
                              onClick={() => {
                                setFormData((prev) => ({
                                  ...prev,
                                  acheteurId: membre.id,
                                  nomAcheteur: undefined,
                                  prenomAcheteur: undefined,
                                  telephoneAcheteur: undefined,
                                  genre: undefined,
                                }));
                                setMembreSearch(
                                  `${membre.nom} ${membre.prenom}`
                                );
                                setFilteredMembres([]);
                              }}
                              className="p-2 hover:bg-gray-100 cursor-pointer"
                            >
                              {membre.nom} {membre.prenom}
                            </li>
                          ))}
                        </ul>
                      )}
                    </div>

                    {!formData.acheteurId && (
                      <>
                        <div>
                          <label className="block text-gray-700">Nom *</label>
                          <input
                            type="text"
                            name="nomAcheteur"
                            value={formData.nomAcheteur || ""}
                            onChange={handleChange}
                            className="w-full p-2 border rounded"
                          />
                        </div>
                        <div>
                          <label className="block text-gray-700">
                            Prénom *
                          </label>
                          <input
                            type="text"
                            name="prenomAcheteur"
                            value={formData.prenomAcheteur || ""}
                            onChange={handleChange}
                            className="w-full p-2 border rounded"
                          />
                        </div>
                        <div>
                          <label className="block text-gray-700">
                            Téléphone *
                          </label>
                          <input
                            type="text"
                            name="telephoneAcheteur"
                            value={formData.telephoneAcheteur || ""}
                            onChange={handleChange}
                            className="w-full p-2 border rounded"
                          />
                        </div>
                        <div>
                          <label className="block text-gray-700">Genre</label>
                          <select
                            name="genre"
                            value={formData.genre || ""}
                            onChange={handleChange}
                            className="w-full p-2 border rounded"
                          >
                            <option value="">Sélectionner</option>
                            <option value="HOMME">Homme</option>
                            <option value="FEMME">Femme</option>
                          </select>
                        </div>
                      </>
                    )}
                    <div>
                      <label className="block text-gray-700">
                        Mode de Paiement *
                      </label>
                      <select
                        name="modeDePaiement"
                        value={formData.modeDePaiement}
                        onChange={handleChange}
                        className="w-full p-2 border rounded"
                        required
                      >
                        <option value="ESPECE">CASH</option>
                        <option value="CARTE">ORANGE_MONEY</option>
                        <option value="MOBILE">WAVE</option>
                      </select>
                    </div>
                  </div>

                  <div className="mt-4">
                    {formData.produitIds.map((produitId, index) => (
                      <div key={index} className="flex space-x-4 mb-2">
                        <select
                          value={produitId || ""}
                          onChange={(e) =>
                            handleProduitChange(
                              index,
                              "produitId",
                              e.target.value
                            )
                          }
                          className="w-1/2 p-2 border rounded"
                          required
                        >
                          <option value="">Sélectionner un produit</option>
                          {produits.map((produit) => (
                            <option key={produit.id} value={produit.id}>
                              {produit.nom} ({produit.prixUnitaire} FCFA)
                            </option>
                          ))}
                        </select>
                        <input
                          type="number"
                          value={formData.quantites[index] || 1}
                          onChange={(e) =>
                            handleProduitChange(
                              index,
                              "quantite",
                              e.target.value
                            )
                          }
                          className="w-1/4 p-2 border rounded"
                          min="1"
                          required
                        />
                        <button
                          type="button"
                          onClick={() => removeProduitField(index)}
                          className="bg-red-500 text-white px-2 py-1 rounded hover:bg-red-600"
                        >
                          Supprimer
                        </button>
                      </div>
                    ))}
                    <button
                      type="button"
                      onClick={addProduitField}
                      className="mt-2 bg-green-500 text-white px-4 py-2 rounded hover:bg-green-600"
                    >
                      Ajouter un produit
                    </button>
                  </div>

                  <div className="flex justify-end space-x-3 mt-6">
                    <button
                      type="button"
                      onClick={closeAddForm}
                      className="bg-black text-orange-500 px-4 py-2 rounded hover:bg-gray-600 transition-colors"
                    >
                      Annuler
                    </button>
                    <button
                      type="submit"
                      disabled={adding}
                      className="bg-orange-500 text-white px-6 py-2 rounded-lg hover:bg-orange-400 transition-colors font-bold"
                    >
                      {adding ? "Enregistrement..." : "Enregistrer"}
                    </button>
                  </div>
                </form>
              )}
            </div>
          </div>
        )}
      </main>
    </div>
  );
}

export default GestionVente;
