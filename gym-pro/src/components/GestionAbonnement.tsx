import { useState, useEffect } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";

interface Membre {
  id: number;
  nom: string;
  prenom: string;
  email: string;
  telephone: string;
  genre: "HOMME" | "FEMME";
  gym?: any;
  gyms?: any[];
}

interface TypeDeService {
  id: number;
  nom: string;
  description: string;
  tarifHomme: number;
  tarifFemme: number;
  tarifUnique: number;
  gym: {
    id: number;
    nom: string;
    adresse: string;
    email: string;
    telephone: string;
  };
}

interface Abonnement {
  id: number;
  membreId: number;
  membre: Membre;
  typeDeService: TypeDeService[];
  prix: number;
  dateDebutAbonnement: string;
  dateFinAbonnement: string;
  statut: "EN_COURS" | "EN_PAUSE" | "EXPIRE" | "RESILIE" | "BIENTOT_EXPIRE";
  modePaiement: string;
  periodAbonnement: string;
  datePauseAbonnement: string | null;
  dateRappelFinAbonnement: string | null;
  enregistrerPar: any;
  nombreDeMois: number;
  prixAbonnement: number;
  types: string;
}

interface FormData {
  membreId: string;
  typeDeServiceId?: number;
  nombreDeMois: string;
  modeDePaiement: string;
  periodAbonnement: string;
  prixAbonnement: number;
  genreMembre: "HOMME" | "FEMME";
}

interface RenouvelerFormData {
  periodAbonnement: string;
  ajoutMois: string;
  nouveauAbonnement: string;
}

interface TableauDeBordProps {
  setIsLoggedIn: (value: boolean) => void;
}

function GestionAbonnement({ setIsLoggedIn }: TableauDeBordProps) {
  const [abonnements, setAbonnements] = useState<Abonnement[]>([]);
  const [membres, setMembres] = useState<Membre[]>([]);
  const [typesService, setTypesService] = useState<TypeDeService[]>([]);
  const [selectedAbonnement, setSelectedAbonnement] =
    useState<Abonnement | null>(null);
  const [showAddForm, setShowAddForm] = useState<boolean>(false);
  const [showDetails, setShowDetails] = useState<boolean>(false);
  const [showRenouvelerForm, setShowRenouvelerForm] = useState<boolean>(false);
  const [loading, setLoading] = useState<boolean>(true);
  const [adding, setAdding] = useState<boolean>(false);
  const [updating, setUpdating] = useState<boolean>(false);
  const [error, setError] = useState<string>("");
  const [success, setSuccess] = useState<string>("");
  const [formData, setFormData] = useState<FormData>({
    membreId: "",
    typeDeServiceId: undefined,
    nombreDeMois: "",
    modeDePaiement: "",
    periodAbonnement: "MENSUEL",
    prixAbonnement: 0,
    genreMembre: "HOMME",
  });
  const [renouvelerFormData, setRenouvelerFormData] =
    useState<RenouvelerFormData>({
      periodAbonnement: "MENSUEL",
      ajoutMois: "1",
      nouveauAbonnement: "",
    });
  const [historique, setHistorique] = useState<Abonnement[]>([]);
  const [showHistorique, setShowHistorique] = useState<boolean>(false);
  const [joursAbsence, setJoursAbsence] = useState<string>("7");
  const [membreSearch, setMembreSearch] = useState<string>("");
  const [filteredMembres, setFilteredMembres] = useState<Membre[]>([]);
  const [query, setQuery] = useState<string>("");
  const [filterTypeService, setFilterTypeService] = useState<string>("");
  const [filterStatut, setFilterStatut] = useState<string>("");

  const navigate = useNavigate();
  const token = localStorage.getItem("token");

  const handleLogout = () => {
    localStorage.removeItem("token");
    setIsLoggedIn(false);
  };

  const fetchAbonnements = async () => {
    try {
      const response = await axios.get<any[]>(
        "http://localhost:8080/api/abonnements",
        {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        }
      );
      console.log("Réponse API abonnements:", response.data);

      const abonnementsData = response.data.map((abonnement: any) => ({
        id: abonnement.id,
        membreId: abonnement.membre?.id || abonnement.membreId,
        membre: abonnement.membre,
        typeDeService: Array.isArray(abonnement.typeDeService)
          ? abonnement.typeDeService
          : abonnement.typeDeService
          ? [abonnement.typeDeService]
          : [],
        prix: abonnement.prix || abonnement.prixAbonnement,
        dateDebutAbonnement: abonnement.dateDebutAbonnement,
        dateFinAbonnement: abonnement.dateFinAbonnement,
        statut: abonnement.statut,
        modePaiement: abonnement.modeDePaiement,
        periodAbonnement: abonnement.periodAbonnement,
        datePauseAbonnement: abonnement.datePauseAbonnement,
        dateRappelFinAbonnement: abonnement.dateRappelFinAbonnement,
        enregistrerPar: abonnement.enregistrerPar,
        nombreDeMois: abonnement.nombreDeMois,
        prixAbonnement: abonnement.prixAbonnement,
        types: abonnement.types,
      }));
      setAbonnements(abonnementsData);
      setLoading(false);
    } catch (err: any) {
      setError("Erreur lors de la récupération des abonnements.");
      setLoading(false);
      console.error("Erreur détaillée:", err.response?.data || err.message);
    }
  };

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
    } catch (err: any) {
      console.error("Erreur lors de la récupération des membres:", err);
    }
  };

  const fetchTypesService = async () => {
    try {
      const response = await axios.get<TypeDeService[]>(
        "http://localhost:8080/api/services",
        {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        }
      );
      setTypesService(response.data);
    } catch (err: any) {
      console.error(
        "Erreur lors de la récupération des types de service:",
        err
      );
    }
  };

  const fetchHistorique = async (membreId: number) => {
    if (!membreId || isNaN(membreId)) {
      setError("ID du membre invalide.");
      console.error("membreId invalide:", membreId);
      return;
    }
    try {
      const response = await axios.get<any[]>(
        `http://localhost:8080/api/abonnements/historique/${membreId}`,
        {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        }
      );
      const historiqueData = response.data.map((abonnement: any) => ({
        id: abonnement.id,
        membreId: abonnement.membreId,
        membre: abonnement.membre,
        typeDeService: Array.isArray(abonnement.typeDeService)
          ? abonnement.typeDeService
          : abonnement.typeDeService
          ? [abonnement.typeDeService]
          : [],
        prix: abonnement.prix || abonnement.prixAbonnement,
        dateDebutAbonnement: abonnement.dateDebutAbonnement,
        dateFinAbonnement: abonnement.dateFinAbonnement,
        statut: abonnement.statut,
        modePaiement: abonnement.modeDePaiement,
        periodAbonnement: abonnement.periodAbonnement,
        datePauseAbonnement: abonnement.datePauseAbonnement,
        dateRappelFinAbonnement: abonnement.dateRappelFinAbonnement,
        enregistrerPar: abonnement.enregistrerPar,
        nombreDeMois: abonnement.nombreDeMois,
        prixAbonnement: abonnement.prixAbonnement,
        types: abonnement.types,
      }));
      setHistorique(historiqueData);
      setShowHistorique(true);
    } catch (err: any) {
      setError("Erreur lors de la récupération de l'historique.");
      console.error("Erreur détaillée:", err.response?.data || err.message);
    }
  };

  const handleAddAbonnement = async (e: React.FormEvent) => {
    e.preventDefault();
    setAdding(true);
    console.log("Soumission du formulaire avec données:", formData);

    try {
      await axios.post(
        "http://localhost:8080/api/abonnements/ajouter",
        {
          membreId: parseInt(formData.membreId),
          typeDeServiceId: formData.typeDeServiceId,
          nombreDeMois: parseInt(formData.nombreDeMois),
          modeDePaiement: formData.modeDePaiement,
          periodAbonnement: formData.periodAbonnement,
          prixAbonnement: formData.prixAbonnement,
        },
        {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        }
      );

      setSuccess("Abonnement ajouté avec succès !");
      setShowAddForm(false);
      setFormData({
        membreId: "",
        typeDeServiceId: undefined,
        nombreDeMois: "",
        modeDePaiement: "",
        periodAbonnement: "MENSUEL",
        prixAbonnement: 0,
        genreMembre: "HOMME",
      });
      fetchAbonnements();
      setTimeout(() => setSuccess(""), 3000);
    } catch (err: any) {
      setError(
        `Erreur lors de l'ajout : ${err.response?.data?.message || err.message}`
      );
    } finally {
      setAdding(false);
    }
  };

  const handleMettreEnPause = async (id: number) => {
    if (!joursAbsence || parseInt(joursAbsence) < 7) {
      setError("Le nombre de jours d'absence doit être d'au moins 7.");
      return;
    }

    setUpdating(true);
    try {
      await axios.put(
        `http://localhost:8080/api/abonnements/pause/${id}?joursAbsence=${joursAbsence}`,
        {},
        {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        }
      );

      setSuccess("Abonnement mis en pause avec succès !");
      setShowDetails(false);
      fetchAbonnements();
      setTimeout(() => setSuccess(""), 3000);
    } catch (err: any) {
      setError(
        `Erreur lors de la mise en pause : ${
          err.response?.data?.message || err.message
        }`
      );
    } finally {
      setUpdating(false);
    }
  };

  const handleReprendre = async (id: number) => {
    setUpdating(true);
    try {
      await axios.put(
        `http://localhost:8080/api/abonnements/reprendre/${id}`,
        {},
        {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        }
      );

      setSuccess("Abonnement repris avec succès !");
      setShowDetails(false);
      fetchAbonnements();
      setTimeout(() => setSuccess(""), 3000);
    } catch (err: any) {
      setError(
        `Erreur lors de la reprise : ${
          err.response?.data?.message || err.message
        }`
      );
    } finally {
      setUpdating(false);
    }
  };

  const handleRenouveler = async (id: number) => {
    setUpdating(true);
    try {
      await axios.put(
        `http://localhost:8080/api/abonnements/renouvellement/${id}`,
        {
          periodAbonnement: renouvelerFormData.periodAbonnement,
          ajoutMois: parseInt(renouvelerFormData.ajoutMois),
          nouveauAbonnement: renouvelerFormData.nouveauAbonnement
            ? parseFloat(renouvelerFormData.nouveauAbonnement)
            : null,
        },
        {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        }
      );

      setSuccess("Abonnement renouvelé avec succès !");
      setShowRenouvelerForm(false);
      setShowDetails(false);
      fetchAbonnements();
      setTimeout(() => setSuccess(""), 3000);
    } catch (err: any) {
      setError(
        `Erreur lors du renouvellement : ${
          err.response?.data?.message || err.message
        }`
      );
    } finally {
      setUpdating(false);
    }
  };

  const handleResilier = async (id: number) => {
    setUpdating(true);
    try {
      await axios.put(
        `http://localhost:8080/api/abonnements/resilier/${id}`,
        {},
        {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        }
      );

      setSuccess("Abonnement résilié avec succès !");
      setShowDetails(false);
      fetchAbonnements();
      setTimeout(() => setSuccess(""), 3000);
    } catch (err: any) {
      setError(
        `Erreur lors de la résiliation : ${
          err.response?.data?.message || err.message
        }`
      );
    } finally {
      setUpdating(false);
    }
  };

  const showAbonnementDetails = async (id: number) => {
    try {
      const response = await axios.get<any>(
        `http://localhost:8080/api/abonnements/get_abonnement_by_id/${id}`,
        {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        }
      );
      const abonnementData = {
        id: response.data.id,
        membreId: response.data.membreId,
        membre: response.data.membre,
        typeDeService: response.data.typeDeService,
        prix: response.data.prix || response.data.prixAbonnement,
        dateDebutAbonnement: response.data.dateDebutAbonnement,
        dateFinAbonnement: response.data.dateFinAbonnement,
        statut: response.data.statut,
        modePaiement: response.data.modeDePaiement,
        periodAbonnement: response.data.periodAbonnement,
        datePauseAbonnement: response.data.datePauseAbonnement,
        dateRappelFinAbonnement: response.data.dateRappelFinAbonnement,
        enregistrerPar: response.data.enregistrerPar,
        nombreDeMois: response.data.nombreDeMois,
        prixAbonnement: response.data.prixAbonnement,
        types: response.data.types,
      };
      setSelectedAbonnement(abonnementData);
      setShowDetails(true);
    } catch (err: any) {
      setError("Erreur lors de la récupération des détails de l'abonnement.");
      console.error("Erreur détaillée:", err.response?.data || err.message);
    }
  };

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
  ) => {
    const { name, value } = e.target;
    setFormData((prev) => {
      const updatedFormData = { ...prev, [name]: value };

      if (name === "membreId") {
        const selectedMembre = membres.find((m) => m.id === parseInt(value));
        if (selectedMembre) {
          updatedFormData.genreMembre = selectedMembre.genre;
        }
      }

      return updatedFormData;
    });
  };

  const handleTypeDeServiceChange = (
    e: React.ChangeEvent<HTMLSelectElement>
  ) => {
    const typeId = parseInt(e.target.value);
    const selectedType = typesService.find((type) => type.id === typeId);
    if (selectedType) {
      const frais =
        formData.genreMembre === "HOMME"
          ? selectedType.tarifHomme
          : selectedType.tarifFemme || selectedType.tarifUnique;
      setFormData((prev) => ({
        ...prev,
        typeDeServiceId: typeId,
        prixAbonnement: frais,
      }));
    }
  };

  const handleRenouvelerChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
  ) => {
    const { name, value } = e.target;
    setRenouvelerFormData((prev) => ({ ...prev, [name]: value }));
  };

  const closeAddForm = () => {
    setShowAddForm(false);
    setFormData({
      membreId: "",
      typeDeServiceId: undefined,
      nombreDeMois: "",
      modeDePaiement: "",
      periodAbonnement: "MENSUEL",
      prixAbonnement: 0,
      genreMembre: "HOMME",
    });
    setError("");
  };

  const closeDetails = () => {
    setShowDetails(false);
    setSelectedAbonnement(null);
  };

  const closeRenouvelerForm = () => {
    setShowRenouvelerForm(false);
    setRenouvelerFormData({
      periodAbonnement: "MENSUEL",
      ajoutMois: "1",
      nouveauAbonnement: "",
    });
  };

  const closeHistorique = () => {
    setShowHistorique(false);
    setHistorique([]);
  };

  const handleSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setQuery(e.target.value);
  };

  const handleFilterTypeServiceChange = (
    e: React.ChangeEvent<HTMLSelectElement>
  ) => {
    setFilterTypeService(e.target.value);
  };

  const handleFilterStatutChange = (
    e: React.ChangeEvent<HTMLSelectElement>
  ) => {
    setFilterStatut(e.target.value);
  };

  const filteredAbonnements = abonnements.filter((abonnement) => {
    const matchesQuery =
      !query ||
      `${abonnement.membre.nom} ${abonnement.membre.prenom} ${
        abonnement.typeDeService[0]?.nom || ""
      } ${abonnement.modePaiement} ${abonnement.dateDebutAbonnement} ${
        abonnement.dateFinAbonnement
      } ${abonnement.statut}`
        .toLowerCase()
        .includes(query.toLowerCase());

    const matchesFilterTypeService =
      !filterTypeService ||
      abonnement.typeDeService.some(
        (service) => service.nom === filterTypeService
      );

    const matchesFilterStatut =
      !filterStatut || abonnement.statut === filterStatut;

    return matchesQuery && matchesFilterTypeService && matchesFilterStatut;
  });

  useEffect(() => {
    fetchAbonnements();
    fetchMembres();
    fetchTypesService();
  }, []);

  if (loading) {
    return (
      <div className="min-h-screen bg-black flex items-center justify-center">
        <div className="text-white text-xl">Chargement des abonnements...</div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-b from-black via-black to-orange-500 flex flex-col">
      <header className="bg-black text-white flex justify-between items-center px-6 py-4 shadow-md z-50">
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
              Autres
            </button>
            <div className="absolute left-0 mt-1 hidden group-hover:block w-64 bg-black border border-orange-600 rounded shadow-lg z-10 text-xs">
              <button className="block w-full text-left px-4 py-2 text-white hover:text-orange-400 cursor-pointer">
                Gestion des membres
              </button>
              <button className="block w-full text-left px-4 py-2 text-white hover:text-orange-400 cursor-pointer">
                Gestion du staff
              </button>
              <button className="block w-full text-left px-4 py-2 text-white hover:text-orange-400 cursor-pointer">
                Transaction
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

      <main className="flex-1 p-6">
        <div className="flex justify-between items-center mb-6">
          <div>
            <h1 className="text-3xl font-bold text-white mb-2">
              Gestion des Abonnements
            </h1>
            <p className="text-gray-400">
              {abonnements.length} abonnement
              {abonnements.length !== 1 ? "s" : ""} dans votre salle de sport
            </p>
          </div>
          <button
            onClick={() => setShowAddForm(true)}
            className="bg-orange-500 text-white px-6 py-2 rounded-lg hover:bg-white hover:text-orange-500 transition-colors font-bold"
          >
            + Nouvel abonnement
          </button>
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

        <div className="flex flex-wrap gap-24 mb-10 px-10">
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
              value={filterTypeService}
              onChange={handleFilterTypeServiceChange}
              className="px-4 py-2 rounded-md border border-gray-300 focus:outline-none focus:ring-2 focus:ring-orange-500 text-black w-64"
            >
              <option value="">Tous les types de service</option>
              {typesService.map((service) => (
                <option key={service.id} value={service.nom}>
                  {service.nom}
                </option>
              ))}
            </select>
            <select
              value={filterStatut}
              onChange={handleFilterStatutChange}
              className="px-4 py-2 rounded-md border border-gray-300 focus:outline-none focus:ring-2 focus:ring-orange-500 text-black w-64"
            >
              <option value="">Tous les statuts</option>
              <option value="EN_COURS">En cours</option>
              <option value="EN_PAUSE">En pause</option>
              <option value="EXPIRE">Expiré</option>
              <option value="RESILIE">Résilié</option>
              <option value="BIENTOT_EXPIRE">Bientôt expiré</option>
            </select>
          </div>
        </div>

        {/* Tableau */}

        <div className="bg-white rounded-lg shadow-lg overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-orange-500 text-white">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider">
                    Client
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider">
                    Nom complet
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider">
                    Type de service
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider">
                    Prix
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider">
                    Date début
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider">
                    Date fin
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider">
                    Statut
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider">
                    Actions
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {filteredAbonnements.map((abonnement) => (
                  <tr
                    key={abonnement.id}
                    className="hover:bg-gray-100 cursor-pointer transition-colors"
                    onClick={() => showAbonnementDetails(abonnement.id)}
                  >
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm font-medium text-gray-900">
                        {abonnement.membre.nom} {abonnement.membre.prenom}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm text-gray-900">
                        {abonnement.membre.nom} {abonnement.membre.prenom}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm text-gray-900">
                        {abonnement.typeDeService.length > 0
                          ? abonnement.typeDeService[0]?.nom || "Non défini"
                          : "Non défini"}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm text-gray-900">
                        {abonnement.prixAbonnement} FCFA
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm text-gray-900">
                        {abonnement.dateDebutAbonnement
                          ? new Date(
                              abonnement.dateDebutAbonnement
                            ).toLocaleDateString("fr-FR")
                          : "Non disponible"}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm text-gray-900">
                        {abonnement.dateFinAbonnement
                          ? new Date(
                              abonnement.dateFinAbonnement
                            ).toLocaleDateString("fr-FR")
                          : "Non disponible"}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span
                        className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${
                          abonnement.statut === "EN_COURS"
                            ? "bg-green-100 text-green-800"
                            : abonnement.statut === "EN_PAUSE"
                            ? "bg-yellow-100 text-yellow-800"
                            : abonnement.statut === "EXPIRE"
                            ? "bg-red-100 text-red-800"
                            : abonnement.statut === "RESILIE"
                            ? "bg-gray-100 text-gray-800"
                            : "bg-blue-100 text-blue-800"
                        }`}
                      >
                        {abonnement.statut}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                      <div className="flex space-x-2">
                        <button
                          onClick={(e) => {
                            e.stopPropagation();
                            setSelectedAbonnement(abonnement);
                            setShowRenouvelerForm(true);
                          }}
                          className="bg-green-500 text-white px-3 py-1 rounded text-xs hover:bg-green-600 transition-colors"
                        >
                          Renouveler
                        </button>
                        <button
                          onClick={(e) => {
                            e.stopPropagation();
                            if (abonnement.membreId) {
                              fetchHistorique(abonnement.membreId);
                            } else {
                              setError(
                                "ID du membre non trouvé pour cet abonnement."
                              );
                              console.error(
                                "membreId est undefined pour l'abonnement:",
                                abonnement
                              );
                            }
                          }}
                          className="bg-gray-500 text-white px-3 py-1 rounded text-xs hover:bg-blue-600 transition-colors"
                        >
                          Historique
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        {filteredAbonnements.length === 0 && (
          <div className="text-center py-8 text-gray-500 bg-white rounded-lg mt-4">
            Aucun abonnement trouvé avec les filtres appliqués.
          </div>
        )}

        <div className="mt-6 flex justify-center">
          <button
            onClick={fetchAbonnements}
            className="bg-orange-500 text-white px-6 py-2 rounded-lg hover:bg-white hover:text-orange-500 transition-colors font-bold"
          >
            Actualiser la liste
          </button>
        </div>

        {showAddForm && (
          <div
            className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
            onClick={(e) => {
              const modal = e.currentTarget.querySelector(".bg-white");
              if (modal && !modal.contains(e.target as Node)) {
                closeAddForm();
              }
            }}
          >
            <div className="bg-white rounded-lg p-6 w-full max-w-md">
              <div className="flex justify-between items-center mb-4">
                <h2 className="text-2xl font-bold">Nouvel Abonnement</h2>
                <button
                  onClick={closeAddForm}
                  className="text-gray-500 hover:text-gray-700 text-2xl"
                >
                  ×
                </button>
              </div>

              <form onSubmit={handleAddAbonnement} className="space-y-4">
                <div>
                  <label className="block text-gray-700">Membre *</label>
                  <input
                    type="text"
                    value={membreSearch || ""}
                    onChange={(e) => {
                      const searchTerm = e.target.value;
                      setMembreSearch(searchTerm);
                      const filteredMembres = membres.filter((membre) =>
                        `${membre.nom} ${membre.prenom} ${membre.email} ${membre.telephone}`
                          .toLowerCase()
                          .includes(searchTerm.toLowerCase())
                      );
                      setFilteredMembres(filteredMembres);
                    }}
                    placeholder="Rechercher un membre..."
                    className="w-full p-2 border rounded mb-2"
                    required
                  />
                  {filteredMembres.length > 0 && (
                    <ul className="border rounded max-h-40 overflow-y-auto">
                      {filteredMembres.map((membre) => (
                        <li
                          key={membre.id}
                          onClick={() => {
                            setFormData((prev) => ({
                              ...prev,
                              membreId: membre.id.toString(),
                            }));
                            setMembreSearch(
                              `${membre.nom} ${membre.prenom} - ${membre.email}`
                            );
                            setFilteredMembres([]);
                          }}
                          className="p-2 hover:bg-gray-100 cursor-pointer"
                        >
                          {membre.nom} {membre.prenom} - {membre.email} (
                          {membre.genre})
                        </li>
                      ))}
                    </ul>
                  )}
                </div>

                <div>
                  <label className="block text-gray-700">
                    Type de service *
                  </label>
                  <select
                    name="typeDeServiceId"
                    value={formData.typeDeServiceId?.toString() || ""}
                    onChange={handleTypeDeServiceChange}
                    className="w-full p-2 border rounded"
                    required
                  >
                    <option value="">Sélectionnez un type de service</option>
                    {typesService.map((service) => (
                      <option key={service.id} value={service.id.toString()}>
                        {service.nom} -{" "}
                        {service.tarifUnique ||
                          `${service.tarifHomme}/${service.tarifFemme}`}{" "}
                        FCFA
                      </option>
                    ))}
                  </select>
                </div>

                <div>
                  <label className="block text-gray-700">
                    Nombre de mois *
                  </label>
                  <input
                    type="number"
                    name="nombreDeMois"
                    value={formData.nombreDeMois}
                    onChange={handleChange}
                    className="w-full p-2 border rounded"
                    required
                  />
                </div>

                <div>
                  <label className="block text-gray-700">Prix abonnement</label>
                  <input
                    type="number"
                    name="prixAbonnement"
                    value={formData.prixAbonnement}
                    readOnly
                    className="w-full p-2 border rounded bg-gray-100"
                  />
                </div>

                <div>
                  <label className="block text-gray-700">
                    Mode de paiement *
                  </label>
                  <select
                    name="modeDePaiement"
                    value={formData.modeDePaiement}
                    onChange={handleChange}
                    className="w-full p-2 border rounded"
                    required
                  >
                    <option value="">Sélectionnez un mode de paiement</option>
                    <option value="CASH">Espèces</option>
                    <option value="ORANGE_MONEY">Orange Money</option>
                    <option value="MOOV_MONEY">Moov Money</option>
                    <option value="WAVE">Wave</option>
                    <option value="SAMA_MONEY">Sama Money</option>
                    <option value="CARTE_BANCAIRE">Carte Bancaire</option>
                  </select>
                </div>

                <div>
                  <label className="block text-gray-700">
                    Période d'abonnement
                  </label>
                  <select
                    name="periodAbonnement"
                    value={formData.periodAbonnement}
                    onChange={handleChange}
                    className="w-full p-2 border rounded"
                  >
                    <option value="JOURNALIER">Journalier</option>
                    <option value="MENSUEL">Mensuel</option>
                    <option value="TRIMESTRIEL">Trimestriel</option>
                    <option value="SEMESTRIEL">Semestriel</option>
                    <option value="ANNUEL">Annuel</option>
                  </select>
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
                    disabled={adding || !formData.membreId}
                    className="bg-orange-500 text-white px-6 py-2 rounded-lg hover:bg-orange-400 transition-colors font-bold"
                  >
                    {adding ? "Ajout en cours..." : "Ajouter"}
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}

        {showDetails && selectedAbonnement && (
          <div
            className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
            onClick={(e) => {
              const modal = e.currentTarget.querySelector(".bg-white");
              if (modal && !modal.contains(e.target as Node)) {
                closeDetails();
              }
            }}
          >
            <div className="bg-white rounded-lg p-6 w-full max-w-2xl">
              <div className="flex justify-between items-center mb-4">
                <h2 className="text-2xl font-bold">Détails de l'Abonnement</h2>
                <button
                  onClick={closeDetails}
                  className="text-gray-500 hover:text-gray-700 text-2xl"
                >
                  ×
                </button>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
                <div>
                  <h3 className="font-semibold text-lg mb-3">
                    Informations du membre
                  </h3>
                  <div className="space-y-2">
                    <p>
                      <strong>Nom complet:</strong>{" "}
                      {selectedAbonnement.membre.nom}{" "}
                      {selectedAbonnement.membre.prenom}
                    </p>
                    <p>
                      <strong>Type de service:</strong>{" "}
                      {selectedAbonnement?.typeDeService.length > 0
                        ? selectedAbonnement.typeDeService[0]?.nom ||
                          "Non défini"
                        : "Non défini"}
                    </p>
                    <p>
                      <strong>Prix:</strong> {selectedAbonnement.prixAbonnement}{" "}
                      FCFA
                    </p>
                  </div>
                </div>

                <div>
                  <h3 className="font-semibold text-lg mb-3">
                    Dates et statut
                  </h3>
                  <div className="space-y-2">
                    <p>
                      <strong>Date de début:</strong>{" "}
                      {selectedAbonnement.dateDebutAbonnement
                        ? new Date(
                            selectedAbonnement.dateDebutAbonnement
                          ).toLocaleDateString("fr-FR")
                        : "Non disponible"}
                    </p>
                    <p>
                      <strong>Date de fin:</strong>{" "}
                      {selectedAbonnement.dateFinAbonnement
                        ? new Date(
                            selectedAbonnement.dateFinAbonnement
                          ).toLocaleDateString("fr-FR")
                        : "Non disponible"}
                    </p>
                    <p>
                      <strong>Statut:</strong>
                      <span
                        className={`ml-2 px-2 py-1 rounded-full text-xs ${
                          selectedAbonnement.statut === "EN_COURS"
                            ? "bg-green-100 text-green-800"
                            : selectedAbonnement.statut === "EN_PAUSE"
                            ? "bg-yellow-100 text-yellow-800"
                            : selectedAbonnement.statut === "EXPIRE"
                            ? "bg-red-100 text-red-800"
                            : selectedAbonnement.statut === "RESILIE"
                            ? "bg-gray-100 text-gray-800"
                            : "bg-blue-100 text-blue-800"
                        }`}
                      >
                        {selectedAbonnement.statut}
                      </span>
                    </p>
                    <p>
                      <strong>Mode de paiement:</strong>{" "}
                      {selectedAbonnement.modePaiement}
                    </p>
                    <p>
                      <strong>Période:</strong>{" "}
                      {selectedAbonnement.periodAbonnement}
                    </p>
                  </div>
                </div>
              </div>

              <div className="border-t border-gray-200 pt-4">
                <h3 className="font-semibold text-lg mb-3">
                  Actions disponibles
                </h3>
                <div className="flex flex-wrap gap-2">
                  {selectedAbonnement.statut === "EN_COURS" && (
                    <>
                      <div className="flex items-center space-x-2">
                        <input
                          type="number"
                          value={joursAbsence}
                          onChange={(e) => setJoursAbsence(e.target.value)}
                          className="w-20 p-1 border rounded"
                          min="7"
                          placeholder="Jours"
                        />
                        <button
                          onClick={() =>
                            handleMettreEnPause(selectedAbonnement.id)
                          }
                          disabled={updating}
                          className="bg-orange-500 text-white px-6 py-2 rounded-lg hover:bg-orange-400 transition-colors font-bold"
                        >
                          Mettre en pause
                        </button>
                      </div>
                      <button
                        onClick={() => handleResilier(selectedAbonnement.id)}
                        disabled={updating}
                        className="bg-orange-500 text-white px-6 py-2 rounded-lg hover:bg-orange-400 transition-colors font-bold"
                      >
                        Résilier
                      </button>
                    </>
                  )}

                  {selectedAbonnement.statut === "EN_PAUSE" && (
                    <button
                      onClick={() => handleReprendre(selectedAbonnement.id)}
                      disabled={updating}
                      className="bg-orange-500 text-white px-6 py-2 rounded-lg hover:bg-orange-400 transition-colors font-bold"
                    >
                      Reprendre
                    </button>
                  )}
                </div>
              </div>
            </div>
          </div>
        )}

        {showRenouvelerForm && selectedAbonnement && (
          <div
            className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
            onClick={(e) => {
              const modal = e.currentTarget.querySelector(".bg-white");
              if (modal && !modal.contains(e.target as Node)) {
                closeRenouvelerForm();
              }
            }}
          >
            <div className="bg-white rounded-lg p-6 w-full max-w-md">
              <div className="flex justify-between items-center mb-4">
                <h2 className="text-2xl font-bold">Renouveler l'Abonnement</h2>
                <button
                  onClick={closeRenouvelerForm}
                  className="text-gray-500 hover:text-gray-700 text-2xl"
                >
                  ×
                </button>
              </div>

              <form
                onSubmit={(e) => {
                  e.preventDefault();
                  handleRenouveler(selectedAbonnement.id);
                }}
                className="space-y-4"
              >
                <div>
                  <label className="block text-gray-700">
                    Période d'abonnement
                  </label>
                  <select
                    name="periodAbonnement"
                    value={renouvelerFormData.periodAbonnement}
                    onChange={handleRenouvelerChange}
                    className="w-full p-2 border rounded"
                  >
                    <option value="JOURNALIER">Journalier</option>
                    <option value="MENSUEL">Mensuel</option>
                    <option value="TRIMESTRIEL">Trimestriel</option>
                    <option value="SEMESTRIEL">Semestriel</option>
                    <option value="ANNUEL">Annuel</option>
                  </select>
                </div>

                <div>
                  <label className="block text-gray-700">
                    Nombre de mois à ajouter *
                  </label>
                  <input
                    type="number"
                    name="ajoutMois"
                    value={renouvelerFormData.ajoutMois}
                    onChange={handleRenouvelerChange}
                    className="w-full p-2 border rounded"
                    required
                    min="1"
                  />
                </div>

                <div>
                  <label className="block text-gray-700">
                    Nouveau prix (optionnel)
                  </label>
                  <input
                    type="number"
                    name="nouveauAbonnement"
                    value={renouvelerFormData.nouveauAbonnement}
                    onChange={handleRenouvelerChange}
                    className="w-full p-2 border rounded"
                    step="0.01"
                    min="0"
                  />
                </div>

                <div className="flex justify-end space-x-3 mt-6">
                  <button
                    type="button"
                    onClick={closeRenouvelerForm}
                    className="bg-black text-orange-500 px-4 py-2 rounded hover:bg-gray-600 transition-colors"
                  >
                    Annuler
                  </button>
                  <button
                    type="submit"
                    disabled={updating}
                    className="bg-orange-500 text-white px-6 py-2 rounded-lg hover:bg-orange-400 transition-colors font-bold"
                  >
                    {updating ? "Renouvellement en cours..." : "Renouveler"}
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}

        {showHistorique && (
          <div
            className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
            onClick={(e) => {
              const modal = e.currentTarget.querySelector(".bg-white");
              if (modal && !modal.contains(e.target as Node)) {
                closeHistorique();
              }
            }}
          >
            <div className="bg-white rounded-lg p-6 w-full max-w-4xl max-h-90vh overflow-y-auto">
              <div className="flex justify-between items-center mb-4">
                <h2 className="text-2xl font-bold">
                  Historique des Abonnements
                </h2>
                <button
                  onClick={closeHistorique}
                  className="text-gray-500 hover:text-gray-700 text-2xl"
                >
                  ×
                </button>
              </div>

              {historique.length === 0 ? (
                <p className="text-gray-500">
                  Aucun historique d'abonnement trouvé.
                </p>
              ) : (
                <div className="overflow-x-auto">
                  <table className="w-full">
                    <thead className="bg-gray-100">
                      <tr>
                        <th className="px-4 py-2 text-left">Type de service</th>
                        <th className="px-4 py-2 text-left">Prix</th>
                        <th className="px-4 py-2 text-left">Date début</th>
                        <th className="px-4 py-2 text-left">Date fin</th>
                        <th className="px-4 py-2 text-left">Statut</th>
                      </tr>
                    </thead>
                    <tbody>
                      {historique.map((abonnement) => (
                        <tr key={abonnement.id} className="border-b">
                          <td className="px-4 py-2">
                            {abonnement.typeDeService.length > 0
                              ? abonnement.typeDeService[0]?.nom || "Non défini"
                              : "Non défini"}
                          </td>
                          <td className="px-4 py-2">
                            {abonnement.prixAbonnement} FCFA
                          </td>
                          <td className="px-4 py-2">
                            {abonnement.dateDebutAbonnement
                              ? new Date(
                                  abonnement.dateDebutAbonnement
                                ).toLocaleDateString("fr-FR")
                              : "Non disponible"}
                          </td>
                          <td className="px-4 py-2">
                            {abonnement.dateFinAbonnement
                              ? new Date(
                                  abonnement.dateFinAbonnement
                                ).toLocaleDateString("fr-FR")
                              : "Non disponible"}
                          </td>
                          <td className="px-4 py-2">
                            <span
                              className={`px-2 py-1 rounded-full text-xs ${
                                abonnement.statut === "EN_COURS"
                                  ? "bg-green-100 text-green-800"
                                  : abonnement.statut === "EN_PAUSE"
                                  ? "bg-yellow-100 text-yellow-800"
                                  : abonnement.statut === "EXPIRE"
                                  ? "bg-red-100 text-red-800"
                                  : abonnement.statut === "RESILIE"
                                  ? "bg-gray-100 text-gray-800"
                                  : "bg-blue-100 text-blue-800"
                              }`}
                            >
                              {abonnement.statut}
                            </span>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}

              <div className="flex justify-end mt-6">
                <button
                  onClick={closeHistorique}
                  className="bg-black text-orange-500 px-4 py-2 rounded hover:bg-gray-600 transition-colors"
                >
                  Fermer
                </button>
              </div>
            </div>
          </div>
        )}
      </main>
    </div>
  );
}

export default GestionAbonnement;
