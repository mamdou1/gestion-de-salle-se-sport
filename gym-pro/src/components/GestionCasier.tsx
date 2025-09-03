import { useState, useEffect } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";

interface Casier {
  id: number;
  numeroDeCasier: string;
  prix: number;
  statut: "DISPONIBLE" | "OCCUPER";
  salle: Salle;
  membre?: User;
  dateDebut?: string;
  dateFin?: string;
}

interface Salle {
  id: number;
  nom: string;
  gym: Gym;
}

interface Gym {
  id: number;
  nom: string;
}

interface User {
  id: number;
  nom: string;
  prenom: string;
  email: string;
  telephone: string;
}

interface TableauDeBordProps {
  setIsLoggedIn: (value: boolean) => void;
}

interface CasierDTO {
  salleId: number;
  staffId: number;
  numeroDeCasier: string;
  prix: number;
}

interface AssignerCasierDTO {
  salleId: number;
  membreId: number;
  prix: number;
  numeroDeCasier: string;
}

const getStaffIdFromToken = (token: string | null): number => {
  if (!token) return 0;
  try {
    const payload = JSON.parse(atob(token.split(".")[1]));
    return payload.staffId || payload.id || 0;
  } catch (error) {
    console.error("Erreur lors du décodage du token:", error);
    return 0;
  }
};

function GestionCasier({ setIsLoggedIn }: TableauDeBordProps) {
  const [casiers, setCasiers] = useState<Casier[]>([]);
  const [salles, setSalles] = useState<Salle[]>([]);
  const [membres, setMembres] = useState<User[]>([]);
  const [selectedSalleId, setSelectedSalleId] = useState<number | null>(null);
  const [selectedGymId, setSelectedGymId] = useState<number | null>(null);
  const [showAddForm, setShowAddForm] = useState<boolean>(false);
  const [showAssignForm, setShowAssignForm] = useState<boolean>(false);
  const [loading, setLoading] = useState<boolean>(true);
  const [initialLoading, setInitialLoading] = useState<boolean>(true);
  const [adding, setAdding] = useState<boolean>(false);
  const [assigning, setAssigning] = useState<boolean>(false);
  const [error, setError] = useState<string>("");
  const [success, setSuccess] = useState<string>("");
  const [formData, setFormData] = useState<CasierDTO>({
    salleId: 0,
    staffId: 0,
    numeroDeCasier: "",
    prix: 0,
  });
  const [assignFormData, setAssignFormData] = useState<AssignerCasierDTO>({
    salleId: 0,
    membreId: 0,
    prix: 0,
    numeroDeCasier: "",
  });
  const [availableCasiers, setAvailableCasiers] = useState<Casier[]>([]);

  const navigate = useNavigate();
  const token =
    localStorage.getItem("authToken") || localStorage.getItem("token");

  const handleLogout = () => {
    localStorage.removeItem("authToken");
    localStorage.removeItem("token");
    setIsLoggedIn(false);
    navigate("/connexion");
  };

  const fetchCasiers = async (
    salleId?: number,
    gymId?: number
  ): Promise<void> => {
    setLoading(true);
    try {
      let url = "http://localhost:8080/api/casiers";
      const params = new URLSearchParams();

      if (salleId) params.append("salleId", salleId.toString());
      if (gymId) params.append("gymId", gymId.toString());

      if (params.toString()) url += `?${params.toString()}`;

      const response = await axios.get<Casier[]>(url, {
        headers: { Authorization: `Bearer ${token}` },
      });
      setCasiers(response.data);
    } catch (err: any) {
      console.error("Erreur fetchCasiers:", err);
      setError("Erreur lors de la récupération des casiers");
    } finally {
      setLoading(false);
    }
  };

  const fetchAvailableCasiers = async (salleId: number): Promise<void> => {
    try {
      const response = await axios.get<Casier[]>(
        `http://localhost:8080/api/casiers/disponibles?salleId=${salleId}`,
        { headers: { Authorization: `Bearer ${token}` } }
      );
      setAvailableCasiers(response.data);
    } catch (err: any) {
      console.error("Erreur fetchAvailableCasiers:", err);
    }
  };

  const fetchSalles = async (): Promise<void> => {
    try {
      const response = await axios.get<Salle[]>(
        "http://localhost:8080/api/salles/gym",
        {
          headers: { Authorization: `Bearer ${token}` },
        }
      );
      setSalles(response.data);
    } catch (err: any) {
      console.error("Erreur fetchSalles:", err);
      setError("Erreur lors du chargement des salles");
    }
  };

  const fetchMembres = async (): Promise<void> => {
    try {
      const response = await axios.get<User[]>(
        "http://localhost:8080/api/users/membres",
        {
          headers: { Authorization: `Bearer ${token}` },
        }
      );
      setMembres(response.data);
    } catch (err: any) {
      console.error("Erreur fetchMembres:", err);
      setError("Erreur lors du chargement des membres");
    }
  };

  const handleAddCasier = async (e: React.FormEvent) => {
    e.preventDefault();
    setAdding(true);
    setError("");
    setSuccess("");

    try {
      const staffId = getStaffIdFromToken(token);
      if (staffId === 0)
        throw new Error("Impossible de récupérer l'ID du staff");

      await axios.post(
        "http://localhost:8080/api/casiers",
        { ...formData, staffId, prix: Number(formData.prix) },
        { headers: { Authorization: `Bearer ${token}` } }
      );

      setSuccess("Casier ajouté avec succès !");
      setShowAddForm(false);
      setFormData({ salleId: 0, staffId: 0, numeroDeCasier: "", prix: 0 });

      if (selectedSalleId) fetchCasiers(selectedSalleId);
      else if (selectedGymId) fetchCasiers(undefined, selectedGymId);
    } catch (err: any) {
      setError(
        err.response?.data?.message || "Erreur lors de l'ajout du casier"
      );
    } finally {
      setAdding(false);
    }
  };

  const handleAssignCasier = async (e: React.FormEvent) => {
    e.preventDefault();
    setAssigning(true);
    setError("");
    setSuccess("");

    try {
      await axios.post(
        "http://localhost:8080/api/casiers/assigner",
        { ...assignFormData, prix: Number(assignFormData.prix) },
        { headers: { Authorization: `Bearer ${token}` } }
      );

      setSuccess("Casier assigné avec succès !");
      setShowAssignForm(false);
      setAssignFormData({
        salleId: 0,
        membreId: 0,
        prix: 0,
        numeroDeCasier: "",
      });

      if (selectedSalleId) fetchCasiers(selectedSalleId);
      else if (selectedGymId) fetchCasiers(undefined, selectedGymId);
    } catch (err: any) {
      setError(
        err.response?.data?.message || "Erreur lors de l'assignation du casier"
      );
    } finally {
      setAssigning(false);
    }
  };

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
  ) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: name === "prix" || name === "salleId" ? Number(value) : value,
    }));
  };

  const handleAssignChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
  ) => {
    const { name, value } = e.target;
    setAssignFormData((prev) => {
      const newData = {
        ...prev,
        [name]:
          name === "prix" || name === "salleId" || name === "membreId"
            ? Number(value)
            : value,
      };
      if (name === "salleId" && value) fetchAvailableCasiers(Number(value));
      return newData;
    });
  };

  const closeAddForm = () => {
    setShowAddForm(false);
    setFormData({ salleId: 0, staffId: 0, numeroDeCasier: "", prix: 0 });
    setError("");
  };

  const closeAssignForm = () => {
    setShowAssignForm(false);
    setAssignFormData({ salleId: 0, membreId: 0, prix: 0, numeroDeCasier: "" });
    setAvailableCasiers([]);
    setError("");
  };

  useEffect(() => {
    if (!token) {
      navigate("/connexion");
      return;
    }

    const fetchInitialData = async () => {
      try {
        await Promise.all([fetchSalles(), fetchMembres()]);
      } catch (error) {
        console.error("Erreur lors du chargement initial:", error);
      } finally {
        setInitialLoading(false);
      }
    };

    fetchInitialData();
  }, [token, navigate]);

  useEffect(() => {
    if (selectedSalleId || selectedGymId) {
      fetchCasiers(selectedSalleId || undefined, selectedGymId || undefined);
    }
  }, [selectedSalleId, selectedGymId]);

  useEffect(() => {
    if (error || success) {
      const timer = setTimeout(() => {
        setError("");
        setSuccess("");
      }, 5000);
      return () => clearTimeout(timer);
    }
  }, [error, success]);

  if (initialLoading) {
    return (
      <div className="min-h-screen bg-black flex items-center justify-center">
        <div className="text-white text-xl">Chargement des données...</div>
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
              Gestion des Casiers
            </h1>
            <p className="text-gray-400">
              {casiers.length} casier{casiers.length !== 1 ? "s" : ""} trouvé
              {casiers.length !== 1 ? "s" : ""}
            </p>
          </div>
          <div className="flex space-x-4">
            <button
              onClick={() => setShowAddForm(true)}
              className="bg-orange-500 text-white px-6 py-2 rounded-lg hover:bg-white hover:text-orange-500 transition-colors font-bold"
            >
              + Ajouter un casier
            </button>
            <button
              onClick={() => setShowAssignForm(true)}
              className="bg-purple-500 font-bold text-white px-4 py-2 rounded-lg hover:bg-white hover:text-purple-500 transition-colors"
            >
              Assigner un casier
            </button>
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

        <div className="mb-4 flex space-x-4">
          <select
            value={selectedSalleId?.toString() || ""}
            onChange={(e) => {
              const value = e.target.value;
              setSelectedSalleId(value ? parseInt(value) : null);
              setSelectedGymId(null);
            }}
            className="p-2 border rounded bg-white"
          >
            <option value="">Sélectionner une salle</option>
            {salles.map((salle) => (
              <option key={salle.id} value={salle.id}>
                {salle.nom} (Gym: {salle.gym.nom})
              </option>
            ))}
          </select>

          <select
            value={selectedGymId?.toString() || ""}
            onChange={(e) => {
              const value = e.target.value;
              setSelectedGymId(value ? parseInt(value) : null);
              setSelectedSalleId(null);
            }}
            className="p-2 border rounded bg-white"
          >
            <option value="">Sélectionner un gym</option>
            {Array.from(new Set(salles.map((s) => s.gym.id)))
              .map((gymId) => {
                const gym = salles.find((s) => s.gym.id === gymId)?.gym;
                return gym ? (
                  <option key={gymId} value={gymId}>
                    {gym.nom}
                  </option>
                ) : null;
              })
              .filter(Boolean)}
          </select>
        </div>

        {loading ? (
          <div className="text-center py-8 text-white">
            Chargement des casiers...
          </div>
        ) : (
          <>
            <div className="bg-white rounded-lg shadow-lg overflow-hidden">
              <table className="w-full">
                <thead className="bg-orange-500 text-white">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider">
                      Numéro
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider">
                      Prix
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider">
                      Statut
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider">
                      Salle
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider">
                      Membre
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider">
                      Date Début
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider">
                      Date Fin
                    </th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {casiers.map((casier) => (
                    <tr key={casier.id} className="hover:bg-gray-100">
                      <td className="px-6 py-4 whitespace-nowrap">
                        {casier.numeroDeCasier}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        {casier.prix.toLocaleString()} FCFA
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span
                          className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${
                            casier.statut === "DISPONIBLE"
                              ? "bg-green-100 text-green-800"
                              : "bg-red-100 text-red-800"
                          }`}
                        >
                          {casier.statut}
                        </span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        {casier.salle.nom}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        {casier.membre
                          ? `${casier.membre.nom} ${casier.membre.prenom}`
                          : "N/A"}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        {casier.dateDebut
                          ? new Date(casier.dateDebut).toLocaleDateString()
                          : "N/A"}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        {casier.dateFin
                          ? new Date(casier.dateFin).toLocaleDateString()
                          : "N/A"}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {casiers.length === 0 && (
              <div className="text-center py-8 text-gray-500 bg-white rounded-lg mt-4">
                Aucun casier trouvé. Veuillez sélectionner une salle ou un gym.
              </div>
            )}
          </>
        )}

        {/* Les modales restent identiques */}
        {showAddForm && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div
              className="bg-white rounded-lg p-6 w-full max-w-md"
              onClick={(e) => e.stopPropagation()}
            >
              {/* ... contenu identique ... */}
            </div>
          </div>
        )}

        {showAssignForm && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div
              className="bg-white rounded-lg p-6 w-full max-w-md"
              onClick={(e) => e.stopPropagation()}
            >
              {/* ... contenu identique ... */}
            </div>
          </div>
        )}
      </main>
    </div>
  );
}

export default GestionCasier;
