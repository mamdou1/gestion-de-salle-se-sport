import { useState, useEffect } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";

interface AbonnementGym {
  id: number;
  gym: Gym;
  nombreDeMois: number;
  prixAbonnement: number;
  modeDePaiement: string;
  periodAbonnement: string;
  dateDebutAbonnement: string;
  dateFinAbonnement: string;
  dateRappelFinAbonnement: string;
  statut: StatutAbonnement;
  datePauseAbonnement?: string;
  joursAbsence?: number;
  dateResiliation?: string;
  enregistrerPar: User;
}

interface Gym {
  id: number;
  nom: string;
  adresse: string;
  email: string;
  telephone: string;
}

interface User {
  id: number;
  nom: string;
  prenom: string;
}

enum StatutAbonnement {
  EN_COURS = "EN_COURS",
  BIENTOT_EXPIRE = "BIENTOT_EXPIRE",
  EXPIRE = "EXPIRE",
  EN_PAUSE = "EN_PAUSE",
  RESILIE = "RESILIE",
}

interface AbonnementGymDTO {
  gymId: number;
  nombreDeMois: number;
  prixAbonnement: number;
  modeDePaiement: string;
  periodAbonnement: string;
}

interface RenouvelerAbonnementDTO {
  ajoutMois: number;
  nouveauAbonnement: number;
}

interface PauseAbonnementDTO {
  joursAbsence: number;
}

interface TableauDeBordProps {
  setIsLoggedIn: (value: boolean) => void;
}

function GestionGymAbonnement({ setIsLoggedIn }: TableauDeBordProps) {
  const [abonnements, setAbonnements] = useState<AbonnementGym[]>([]);
  const [gyms, setGyms] = useState<Gym[]>([]);
  const [selectedAbonnement, setSelectedAbonnement] =
    useState<AbonnementGym | null>(null);
  const [showAddForm, setShowAddForm] = useState<boolean>(false);
  const [showRenewForm, setShowRenewForm] = useState<boolean>(false);
  const [showPauseForm, setShowPauseForm] = useState<boolean>(false);
  const [loading, setLoading] = useState<boolean>(true);
  const [actionLoading, setActionLoading] = useState<boolean>(false);
  const [error, setError] = useState<string>("");
  const [success, setSuccess] = useState<string>("");

  const [formData, setFormData] = useState<AbonnementGymDTO>({
    gymId: 0,
    nombreDeMois: 1,
    prixAbonnement: 0,
    modeDePaiement: "CASH",
    periodAbonnement: "MENSUEL",
  });

  const [renewData, setRenewData] = useState<RenouvelerAbonnementDTO>({
    ajoutMois: 1,
    nouveauAbonnement: 0,
  });

  const [pauseData, setPauseData] = useState<PauseAbonnementDTO>({
    joursAbsence: 7,
  });

  const navigate = useNavigate();
  const token =
    localStorage.getItem("authToken") || localStorage.getItem("token");

  const handleLogout = () => {
    localStorage.removeItem("authToken");
    localStorage.removeItem("token");
    setIsLoggedIn(false);
    navigate("/connexion");
  };

  const fetchAbonnements = async () => {
    setLoading(true);
    try {
      const response = await axios.get<any>(
        "http://localhost:8080/api/abonnementsGyms",
        {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        }
      );
      console.log("Réponse API abonnements brute (statut):", response.status);
      console.log("Réponse API abonnements brute (data):", response.data);

      const abonnementsData = Array.isArray(response.data)
        ? response.data.map((item: any) => ({
            id: item.id,
            gym: item.gym || {
              id: 0,
              nom: "Inconnu",
              adresse: "",
              email: "",
              telephone: "",
            },
            nombreDeMois: item.nombreDeMois || 0,
            prixAbonnement: item.prixAbonnement || 0,
            modeDePaiement: item.modeDePaiement || "CASH",
            periodAbonnement: item.periodAbonnement || "MENSUEL",
            dateDebutAbonnement: item.dateDebutAbonnement || "",
            dateFinAbonnement: item.dateFinAbonnement || "",
            dateRappelFinAbonnement: item.dateRappelFinAbonnement || "",
            statut: item.statut || StatutAbonnement.EN_COURS,
            datePauseAbonnement: item.datePauseAbonnement || undefined,
            joursAbsence: item.joursAbsence || undefined,
            dateResiliation: item.dateResiliation || undefined,
            enregistrerPar: item.enregistrerPar || {
              id: 0,
              nom: "",
              prenom: "",
            },
          }))
        : [];
      setAbonnements(abonnementsData);
    } catch (err: any) {
      console.error(
        "Erreur détaillée lors de fetchAbonnements:",
        err.response?.data || err.message,
        "Statut:",
        err.response?.status
      );
      setError("Erreur lors de la récupération des abonnements.");
      setAbonnements([]);
    } finally {
      setLoading(false);
    }
  };

  const fetchGyms = async () => {
    try {
      const response = await axios.get<Gym[]>(
        "http://localhost:8080/api/gyms",
        {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        }
      );
      console.log("Gyms récupérés:", response.data);
      setGyms(response.data);
    } catch (err: any) {
      console.error("Erreur lors de la récupération des gyms:", err);
    }
  };

  const handleAddAbonnement = async (e: React.FormEvent) => {
    e.preventDefault();
    setActionLoading(true);
    setError("");
    setSuccess("");

    try {
      await axios.post(
        "http://localhost:8080/api/abonnementsGyms/ajouter",
        formData,
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
        gymId: 0,
        nombreDeMois: 1,
        prixAbonnement: 0,
        modeDePaiement: "CASH",
        periodAbonnement: "MENSUEL",
      });
      fetchAbonnements();
    } catch (err: any) {
      setError(
        err.response?.data?.message || "Erreur lors de l'ajout de l'abonnement"
      );
    } finally {
      setActionLoading(false);
    }
  };

  const handleRenewAbonnement = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedAbonnement) return;

    setActionLoading(true);
    setError("");
    setSuccess("");

    try {
      await axios.post(
        `http://localhost:8080/api/abonnementsGyms/renouvellement/${selectedAbonnement.id}`,
        renewData,
        {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        }
      );

      setSuccess("Abonnement renouvelé avec succès !");
      setShowRenewForm(false);
      setRenewData({ ajoutMois: 1, nouveauAbonnement: 0 });
      fetchAbonnements();
    } catch (err: any) {
      setError(err.response?.data?.message || "Erreur lors du renouvellement");
    } finally {
      setActionLoading(false);
    }
  };

  const handlePauseAbonnement = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedAbonnement) return;

    setActionLoading(true);
    setError("");
    setSuccess("");

    try {
      await axios.put(
        `http://localhost:8080/api/abonnementsGyms/pause/${selectedAbonnement.id}`,
        pauseData,
        {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        }
      );

      setSuccess("Abonnement mis en pause avec succès !");
      setShowPauseForm(false);
      setPauseData({ joursAbsence: 7 });
      fetchAbonnements();
    } catch (err: any) {
      setError(
        err.response?.data?.message || "Erreur lors de la mise en pause"
      );
    } finally {
      setActionLoading(false);
    }
  };

  const handleResumeAbonnement = async (id: number) => {
    setActionLoading(true);
    setError("");
    setSuccess("");

    try {
      await axios.put(
        `http://localhost:8080/api/abonnementsGyms/reprendre/${id}`,
        {},
        {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        }
      );

      setSuccess("Abonnement repris avec succès !");
      fetchAbonnements();
    } catch (err: any) {
      setError(err.response?.data?.message || "Erreur lors de la reprise");
    } finally {
      setActionLoading(false);
    }
  };

  const handleCancelAbonnement = async (id: number) => {
    setActionLoading(true);
    setError("");
    setSuccess("");

    try {
      await axios.put(
        `http://localhost:8080/api/abonnementsGyms/resilier/${id}`,
        {},
        {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        }
      );

      setSuccess("Abonnement résilié avec succès !");
      fetchAbonnements();
    } catch (err: any) {
      setError(err.response?.data?.message || "Erreur lors de la résiliation");
    } finally {
      setActionLoading(false);
    }
  };

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
  ) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]:
        name === "gymId" || name === "nombreDeMois"
          ? Number(value)
          : name === "prixAbonnement"
          ? parseFloat(value)
          : value,
    }));
  };

  const handleRenewChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
  ) => {
    const { name, value } = e.target;
    setRenewData((prev) => ({
      ...prev,
      [name]: name === "ajoutMois" ? Number(value) : parseFloat(value),
    }));
  };

  const handlePauseChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setPauseData({ joursAbsence: Number(e.target.value) });
  };

  const closeAddForm = () => {
    setShowAddForm(false);
    setFormData({
      gymId: 0,
      nombreDeMois: 1,
      prixAbonnement: 0,
      modeDePaiement: "CASH",
      periodAbonnement: "MENSUEL",
    });
    setError("");
  };

  const closeRenewForm = () => {
    setShowRenewForm(false);
    setRenewData({ ajoutMois: 1, nouveauAbonnement: 0 });
    setError("");
  };

  const closePauseForm = () => {
    setShowPauseForm(false);
    setPauseData({ joursAbsence: 7 });
    setError("");
  };

  const getStatusColor = (statut: StatutAbonnement) => {
    switch (statut) {
      case StatutAbonnement.EN_COURS:
        return "bg-green-100 text-green-800";
      case StatutAbonnement.BIENTOT_EXPIRE:
        return "bg-yellow-100 text-yellow-800";
      case StatutAbonnement.EXPIRE:
        return "bg-red-100 text-red-800";
      case StatutAbonnement.EN_PAUSE:
        return "bg-blue-100 text-blue-800";
      case StatutAbonnement.RESILIE:
        return "bg-gray-100 text-gray-800";
      default:
        return "bg-gray-100 text-gray-800";
    }
  };

  useEffect(() => {
    const fetchData = async () => {
      await fetchGyms(); // Charge les gyms en premier
      await fetchAbonnements(); // Puis les abonnements
    };
    fetchData();
  }, []);

  useEffect(() => {
    if (error || success) {
      const timer = setTimeout(() => {
        setError("");
        setSuccess("");
      }, 5000);
      return () => clearTimeout(timer);
    }
  }, [error, success]);

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
              Gestion des Abonnements Gym
            </h1>
            <p className="text-gray-400">
              {abonnements.length} abonnement
              {abonnements.length !== 1 ? "s" : ""} trouvé
              {abonnements.length !== 1 ? "s" : ""}
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

        <div className="bg-white rounded-lg shadow-lg overflow-hidden">
          <table className="w-full">
            <thead className="bg-orange-500 text-white">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider">
                  Gym
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider">
                  Prix
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider">
                  Durée
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider">
                  Début
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider">
                  Fin
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
              {abonnements.map((abonnement) => (
                <tr key={abonnement.id} className="hover:bg-gray-100">
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="text-sm font-medium text-gray-900">
                      {abonnement.gym.nom}
                    </div>
                    <div className="text-sm text-gray-500">
                      {abonnement.gym.adresse}
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="text-sm text-gray-900">
                      {abonnement.prixAbonnement?.toLocaleString()} FCFA
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="text-sm text-gray-900">
                      {abonnement.nombreDeMois} mois
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="text-sm text-gray-900">
                      {abonnement.dateDebutAbonnement
                        ? new Date(
                            abonnement.dateDebutAbonnement
                          ).toLocaleDateString()
                        : "N/A"}
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="text-sm text-gray-900">
                      {abonnement.dateFinAbonnement
                        ? new Date(
                            abonnement.dateFinAbonnement
                          ).toLocaleDateString()
                        : "N/A"}
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span
                      className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${getStatusColor(
                        abonnement.statut
                      )}`}
                    >
                      {abonnement.statut}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                    <div className="flex space-x-2">
                      <button
                        onClick={() => {
                          setSelectedAbonnement(abonnement);
                          setShowRenewForm(true);
                        }}
                        className="text-blue-600 hover:text-blue-900"
                        disabled={actionLoading}
                      >
                        Renouveler
                      </button>
                      {abonnement.statut === StatutAbonnement.EN_COURS && (
                        <button
                          onClick={() => {
                            setSelectedAbonnement(abonnement);
                            setShowPauseForm(true);
                          }}
                          className="text-yellow-600 hover:text-yellow-900"
                          disabled={actionLoading}
                        >
                          Pause
                        </button>
                      )}
                      {abonnement.statut === StatutAbonnement.EN_PAUSE && (
                        <button
                          onClick={() => handleResumeAbonnement(abonnement.id)}
                          className="text-green-600 hover:text-green-900"
                          disabled={actionLoading}
                        >
                          Reprendre
                        </button>
                      )}
                      {abonnement.statut === StatutAbonnement.EN_COURS && (
                        <button
                          onClick={() => handleCancelAbonnement(abonnement.id)}
                          className="text-red-600 hover:text-red-900"
                          disabled={actionLoading}
                        >
                          Résilier
                        </button>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {abonnements.length === 0 && (
          <div className="text-center py-8 text-gray-500 bg-white rounded-lg mt-4">
            Aucun abonnement trouvé.
          </div>
        )}

        {showAddForm && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div className="bg-white rounded-lg p-6 w-full max-w-md">
              <div className="flex justify-between items-center mb-4">
                <h2 className="text-2xl font-bold">Nouvel Abonnement Gym</h2>
                <button
                  onClick={closeAddForm}
                  className="text-gray-500 hover:text-gray-700 text-2xl"
                >
                  ×
                </button>
              </div>
              <form onSubmit={handleAddAbonnement} className="space-y-4">
                <div>
                  <label className="block text-gray-700">Gym *</label>
                  <select
                    name="gymId"
                    value={formData.gymId}
                    onChange={handleChange}
                    className="w-full p-2 border rounded"
                    required
                  >
                    <option value="">Sélectionner un gym</option>
                    {gyms.map((gym) => (
                      <option key={gym.id} value={gym.id}>
                        {gym.nom} - {gym.adresse}
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
                    min="1"
                    required
                  />
                </div>
                <div>
                  <label className="block text-gray-700">Prix (FCFA) *</label>
                  <input
                    type="number"
                    name="prixAbonnement"
                    value={formData.prixAbonnement}
                    onChange={handleChange}
                    className="w-full p-2 border rounded"
                    step="1000"
                    min="0"
                    required
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
                    <option value="CASH">Cash</option>
                    <option value="CARTE">Carte</option>
                    <option value="VIREMENT">Virement</option>
                    <option value="MOBILE_MONEY">Mobile Money</option>
                  </select>
                </div>
                <div>
                  <label className="block text-gray-700">Période *</label>
                  <select
                    name="periodAbonnement"
                    value={formData.periodAbonnement}
                    onChange={handleChange}
                    className="w-full p-2 border rounded"
                    required
                  >
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
                    className="bg-gray-500 text-white px-4 py-2 rounded hover:bg-gray-600 transition-colors"
                  >
                    Annuler
                  </button>
                  <button
                    type="submit"
                    disabled={actionLoading}
                    className="bg-orange-500 text-white px-6 py-2 rounded-lg hover:bg-orange-400 transition-colors font-bold disabled:opacity-50"
                  >
                    {actionLoading ? "Ajout..." : "Ajouter"}
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}

        {showRenewForm && selectedAbonnement && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div className="bg-white rounded-lg p-6 w-full max-w-md">
              <div className="flex justify-between items-center mb-4">
                <h2 className="text-2xl font-bold">Renouveler Abonnement</h2>
                <button
                  onClick={closeRenewForm}
                  className="text-gray-500 hover:text-gray-700 text-2xl"
                >
                  ×
                </button>
              </div>
              <form onSubmit={handleRenewAbonnement} className="space-y-4">
                <div>
                  <label className="block text-gray-700">
                    Mois à ajouter *
                  </label>
                  <input
                    type="number"
                    name="ajoutMois"
                    value={renewData.ajoutMois}
                    onChange={handleRenewChange}
                    className="w-full p-2 border rounded"
                    min="1"
                    required
                  />
                </div>
                <div>
                  <label className="block text-gray-700">
                    Nouveau prix (optionnel)
                  </label>
                  <input
                    type="number"
                    name="nouveauAbonnement"
                    value={renewData.nouveauAbonnement}
                    onChange={handleRenewChange}
                    className="w-full p-2 border rounded"
                    step="1000"
                    min="0"
                  />
                </div>
                <div className="flex justify-end space-x-3 mt-6">
                  <button
                    type="button"
                    onClick={closeRenewForm}
                    className="bg-gray-500 text-white px-4 py-2 rounded hover:bg-gray-600 transition-colors"
                  >
                    Annuler
                  </button>
                  <button
                    type="submit"
                    disabled={actionLoading}
                    className="bg-orange-500 text-white px-6 py-2 rounded-lg hover:bg-orange-400 transition-colors font-bold disabled:opacity-50"
                  >
                    {actionLoading ? "Renouvellement..." : "Renouveler"}
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}

        {showPauseForm && selectedAbonnement && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div className="bg-white rounded-lg p-6 w-full max-w-md">
              <div className="flex justify-between items-center mb-4">
                <h2 className="text-2xl font-bold">Mettre en Pause</h2>
                <button
                  onClick={closePauseForm}
                  className="text-gray-500 hover:text-gray-700 text-2xl"
                >
                  ×
                </button>
              </div>
              <form onSubmit={handlePauseAbonnement} className="space-y-4">
                <div>
                  <label className="block text-gray-700">
                    Jours d'absence * (min. 7)
                  </label>
                  <input
                    type="number"
                    value={pauseData.joursAbsence}
                    onChange={handlePauseChange}
                    className="w-full p-2 border rounded"
                    min="7"
                    required
                  />
                </div>
                <div className="flex justify-end space-x-3 mt-6">
                  <button
                    type="button"
                    onClick={closePauseForm}
                    className="bg-gray-500 text-white px-4 py-2 rounded hover:bg-gray-600 transition-colors"
                  >
                    Annuler
                  </button>
                  <button
                    type="submit"
                    disabled={actionLoading}
                    className="bg-orange-500 text-white px-6 py-2 rounded-lg hover:bg-orange-400 transition-colors font-bold disabled:opacity-50"
                  >
                    {actionLoading ? "Mise en pause..." : "Mettre en pause"}
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}
      </main>
    </div>
  );
}

export default GestionGymAbonnement;
