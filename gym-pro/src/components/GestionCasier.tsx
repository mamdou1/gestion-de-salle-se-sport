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
  const [selectedCasierId, setSelectedCasierId] = useState<number | null>(null);
  const [casierDetails, setCasierDetails] = useState<Casier | null>(null);
  const [query, setQuery] = useState<string>("");
  const [filterStatut, setFilterStatut] = useState<string>("");
  const [filterSalleId, setFilterSalleId] = useState<number>(0); // Nouvel état pour le filtre par salle
  const [currentPage, setCurrentPage] = useState<number>(1);
  const [itemsPerPage, setItemsPerPage] = useState<number>(5);

  const navigate = useNavigate();
  const token =
    localStorage.getItem("authToken") || localStorage.getItem("token");

  const handleLogout = () => {
    localStorage.removeItem("authToken");
    localStorage.removeItem("token");
    setIsLoggedIn(false);
    navigate("/connexion");
  };

  const fetchCasiers = async () => {
    setLoading(true);
    try {
      const url = "http://localhost:8080/api/casiers";
      const response = await axios.get<Casier[]>(url, {
        headers: { Authorization: `Bearer ${token}` },
      });
      setCasiers(response.data);
      setError("");
    } catch (err: any) {
      console.error("Erreur fetchCasiers:", err.response?.data || err.message);
      setError(
        err.response?.status === 404
          ? "Endpoint non trouvé. Vérifiez l'ID du gym."
          : err.response?.data?.message ||
              "Erreur lors de la récupération des casiers"
      );
    } finally {
      setLoading(false);
    }
  };

  const fetchCasierById = async (casierId: number): Promise<void> => {
    try {
      const response = await axios.get<Casier>(
        `http://localhost:8080/api/casiers/${casierId}`,
        { headers: { Authorization: `Bearer ${token}` } }
      );
      setCasierDetails(response.data);
      setError("");
    } catch (err: any) {
      console.error(
        "Erreur fetchCasierById:",
        err.response?.data || err.message
      );
      setError("Erreur lors de la récupération des détails du casier");
    }
  };

  const fetchSalles = async (): Promise<void> => {
    try {
      const response = await axios.get<Salle[]>(
        "http://localhost:8080/api/salles/gym",
        { headers: { Authorization: `Bearer ${token}` } }
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
        "http://localhost:8080/api/users/membre",
        { headers: { Authorization: `Bearer ${token}` } }
      );
      setMembres(response.data);
      console.log("Membres récupérés:", response.data);
    } catch (err: any) {
      setError(
        "Erreur lors de la récupération des membres. Vous pouvez toujours assigner à un non-membre."
      );
      setMembres([]);
      console.error(
        "Erreur détaillée (membres):",
        err.response?.data || err.message
      );
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
        "http://localhost:8080/api/casiers/ajouter",
        { ...formData, staffId, prix: Number(formData.prix) },
        { headers: { Authorization: `Bearer ${token}` } }
      );

      setSuccess("Casier ajouté avec succès !");
      setShowAddForm(false);
      fetchCasiers();
      setFormData({ salleId: 0, staffId: 0, numeroDeCasier: "", prix: 0 });
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
      fetchCasiers();
      setAssignFormData({
        salleId: 0,
        membreId: 0,
        prix: 0,
        numeroDeCasier: "",
      });
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
    setAssignFormData((prev) => ({
      ...prev,
      [name]:
        name === "prix" || name === "salleId" || name === "membreId"
          ? Number(value)
          : value,
    }));
  };

  const closeAddForm = () => {
    setShowAddForm(false);
    setFormData({ salleId: 0, staffId: 0, numeroDeCasier: "", prix: 0 });
    setError("");
  };

  const closeAssignForm = () => {
    setShowAssignForm(false);
    setAssignFormData({ salleId: 0, membreId: 0, prix: 0, numeroDeCasier: "" });
    setError("");
  };

  const handleViewDetails = (casierId: number) => {
    setSelectedCasierId(casierId);
    fetchCasierById(casierId);
  };

  const getMemberName = (membre: User | null | undefined): string => {
    if (!membre) return "N/A";
    return `${membre.nom || ""} ${membre.prenom || ""}`.trim() || "N/A";
  };

  const handleSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setQuery(e.target.value);
    setCurrentPage(1); // Réinitialiser la page lors d'une nouvelle recherche
  };

  const handleFilterStatutChange = (
    e: React.ChangeEvent<HTMLSelectElement>
  ) => {
    setFilterStatut(e.target.value);
    setCurrentPage(1); // Réinitialiser la page lors d'un changement de filtre
  };

  const handleFilterSalleChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    setFilterSalleId(Number(e.target.value));
    setCurrentPage(1); // Réinitialiser la page lors d'un changement de filtre
  };

  const filteredCasiers = casiers.filter((casier) => {
    const numero = casier.numeroDeCasier || ""; // Traite null/undefined comme chaîne vide
    const matchesQuery =
      !query || numero.toLowerCase().includes(query.toLowerCase());
    const matchesFilterStatut = !filterStatut || casier.statut === filterStatut;
    const matchesFilterSalle =
      !filterSalleId || casier.salle?.id === filterSalleId; // Ajout de ?. pour éviter l'erreur
    return matchesQuery && matchesFilterStatut && matchesFilterSalle;
  });

  const indexOfLastItem = currentPage * itemsPerPage;
  const indexOfFirstItem = indexOfLastItem - itemsPerPage;
  const currentCasiers = filteredCasiers.slice(
    indexOfFirstItem,
    indexOfLastItem
  );
  const totalPages = Math.ceil(filteredCasiers.length / itemsPerPage);

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
    if (error || success) {
      const timer = setTimeout(() => {
        setError("");
        setSuccess("");
      }, 5000);
      return () => clearTimeout(timer);
    }
  }, [error, success]);

  useEffect(() => {
    fetchCasiers();
  }, []);

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

        <div className="flex flex-wrap gap-24 mb-10 px-10">
          <form
            onSubmit={(e) => e.preventDefault()}
            className="flex items-center space-x-2"
          >
            <input
              type="text"
              placeholder="Rechercher par numéro de casier..."
              value={query}
              onChange={handleSearchChange}
              className="px-4 py-2 rounded-md border border-gray-300 focus:outline-none focus:ring-2 focus:ring-orange-500 text-black w-64"
            />
          </form>
          <div className="flex items-center space-x-2">
            <select
              value={filterStatut}
              onChange={handleFilterStatutChange}
              className="px-4 py-2 rounded-md border border-gray-300 focus:outline-none focus:ring-2 focus:ring-orange-500 text-black w-64"
            >
              <option value="">Tous les statuts</option>
              <option value="DISPONIBLE">Disponible</option>
              <option value="OCCUPER">Occupé</option>
            </select>
          </div>
          <div className="flex items-center space-x-2">
            <select
              value={filterSalleId}
              onChange={handleFilterSalleChange}
              className="px-4 py-2 rounded-md border border-gray-300 focus:outline-none focus:ring-2 focus:ring-orange-500 text-black w-64"
            >
              <option value={0}>Toutes les salles</option>
              {salles.map((salle) => (
                <option key={salle.id} value={salle.id}>
                  {salle.nom} (Gym: {salle.gym.nom})
                </option>
              ))}
            </select>
          </div>
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
                    <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider">
                      Actions
                    </th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {currentCasiers.map((casier) => (
                    <tr
                      key={casier.id}
                      className="hover:bg-gray-300 cursor-pointer transition-colors odd:bg-gray-100 even:bg-gray-200"
                    >
                      <td className="px-6 py-4 whitespace-nowrap">
                        {casier.numeroDeCasier || "N/A"}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        {casier.prix
                          ? `${casier.prix.toLocaleString()} FCFA`
                          : "N/A"}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span
                          className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${
                            casier.statut === "DISPONIBLE"
                              ? "bg-green-100 text-green-800"
                              : "bg-red-100 text-red-800"
                          }`}
                        >
                          {casier.statut || "N/A"}
                        </span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        {casier.salle?.nom || "N/A"}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        {getMemberName(casier.membre)}
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
                      <td className="px-6 py-4 whitespace-nowrap">
                        <button
                          onClick={() => handleViewDetails(casier.id)}
                          className="bg-blue-500 text-white px-2 py-1 rounded hover:bg-blue-600"
                        >
                          Détails
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {filteredCasiers.length === 0 && (
              <div className="text-center py-8 text-gray-500 bg-white rounded-lg mt-4">
                Aucun casier trouvé avec les filtres appliqués.
              </div>
            )}

            {/* Pagination */}
            {filteredCasiers.length > itemsPerPage && (
              <div className="mt-6 flex justify-center space-x-2">
                <button
                  onClick={() =>
                    setCurrentPage((prev) => Math.max(prev - 1, 1))
                  }
                  disabled={currentPage === 1}
                  className="px-4 py-2 bg-orange-500 text-white rounded-lg hover:bg-orange-600 disabled:bg-gray-400 disabled:cursor-not-allowed"
                >
                  Précédent
                </button>
                {Array.from({ length: totalPages }, (_, i) => i + 1).map(
                  (page) => (
                    <button
                      key={page}
                      onClick={() => setCurrentPage(page)}
                      className={`px-4 py-2 rounded-lg ${
                        currentPage === page
                          ? "bg-orange-600 text-white"
                          : "bg-white text-orange-500 hover:bg-gray-200"
                      }`}
                    >
                      {page}
                    </button>
                  )
                )}
                <button
                  onClick={() => setCurrentPage((prev) => prev + 1)}
                  disabled={currentPage === totalPages}
                  className="px-4 py-2 bg-orange-500 text-white rounded-lg hover:bg-orange-600 disabled:bg-gray-400 disabled:cursor-not-allowed"
                >
                  Suivant
                </button>
              </div>
            )}
          </>
        )}

        {/* Modal pour ajouter un casier */}
        {showAddForm && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div
              className="bg-white rounded-lg p-6 w-full max-w-md"
              onClick={(e) => e.stopPropagation()}
            >
              <h2 className="text-xl font-bold mb-4">Ajouter un casier</h2>
              <form onSubmit={handleAddCasier}>
                <div className="mb-4">
                  <label className="block text-sm font-medium mb-1">
                    Salle
                  </label>
                  <select
                    name="salleId"
                    value={formData.salleId}
                    onChange={handleChange}
                    className="w-full p-2 border rounded"
                    required
                  >
                    <option value="">Sélectionner une salle</option>
                    {salles.map((salle) => (
                      <option key={salle.id} value={salle.id}>
                        {salle.nom} (Gym: {salle.gym.nom})
                      </option>
                    ))}
                  </select>
                </div>
                <div className="mb-4">
                  <label className="block text-sm font-medium mb-1">
                    Numéro
                  </label>
                  <input
                    type="text"
                    name="numeroDeCasier"
                    value={formData.numeroDeCasier}
                    onChange={handleChange}
                    className="w-full p-2 border rounded"
                    required
                  />
                </div>
                <div className="mb-4">
                  <label className="block text-sm font-medium mb-1">Prix</label>
                  <input
                    type="number"
                    name="prix"
                    value={formData.prix}
                    onChange={handleChange}
                    className="w-full p-2 border rounded"
                    required
                  />
                </div>
                <div className="flex justify-end">
                  <button
                    type="button"
                    onClick={closeAddForm}
                    className="mr-2 px-4 py-2 bg-gray-300 rounded"
                  >
                    Annuler
                  </button>
                  <button
                    type="submit"
                    disabled={adding}
                    className="px-4 py-2 bg-orange-500 text-white rounded hover:bg-orange-600"
                  >
                    {adding ? "Ajout..." : "Ajouter"}
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}

        {/* Modal pour assigner un casier */}
        {showAssignForm && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div
              className="bg-white rounded-lg p-6 w-full max-w-md"
              onClick={(e) => e.stopPropagation()}
            >
              <h2 className="text-xl font-bold mb-4">Assigner un casier</h2>
              <form onSubmit={handleAssignCasier}>
                <div className="mb-4">
                  <label className="block text-sm font-medium mb-1">
                    Salle
                  </label>
                  <select
                    name="salleId"
                    value={assignFormData.salleId}
                    onChange={handleAssignChange}
                    className="w-full p-2 border rounded"
                    required
                  >
                    <option value="">Sélectionner une salle</option>
                    {salles.map((salle) => (
                      <option key={salle.id} value={salle.id}>
                        {salle.nom} (Gym: {salle.gym.nom})
                      </option>
                    ))}
                  </select>
                </div>
                <div className="mb-4">
                  <label className="block text-sm font-medium mb-1">
                    Membre
                  </label>
                  <select
                    name="membreId"
                    value={assignFormData.membreId}
                    onChange={handleAssignChange}
                    className="w-full p-2 border rounded"
                  >
                    <option value="">Sélectionner un membre</option>
                    {membres.map((membre) => (
                      <option key={membre.id} value={membre.id}>
                        {membre.nom} {membre.prenom}
                      </option>
                    ))}
                  </select>
                </div>
                <div className="mb-4">
                  <label className="block text-sm font-medium mb-1">Prix</label>
                  <input
                    type="number"
                    name="prix"
                    value={assignFormData.prix}
                    onChange={handleAssignChange}
                    className="w-full p-2 border rounded"
                    required
                  />
                </div>
                <div className="flex justify-end">
                  <button
                    type="button"
                    onClick={closeAssignForm}
                    className="mr-2 px-4 py-2 bg-gray-300 rounded"
                  >
                    Annuler
                  </button>
                  <button
                    type="submit"
                    disabled={assigning}
                    className="px-4 py-2 bg-purple-500 text-white rounded hover:bg-purple-600"
                  >
                    {assigning ? "Assignation..." : "Assigner"}
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}

        {/* Modal pour les détails d'un casier */}
        {selectedCasierId && casierDetails && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div
              className="bg-white rounded-lg p-6 w-full max-w-md"
              onClick={(e) => e.stopPropagation()}
            >
              <h2 className="text-xl font-bold mb-4">Détails du Casier</h2>
              <p>
                <strong>Numéro :</strong> {casierDetails.numeroDeCasier}
              </p>
              <p>
                <strong>Prix :</strong> {casierDetails.prix.toLocaleString()}{" "}
                FCFA
              </p>
              <p>
                <strong>Statut :</strong> {casierDetails.statut}
              </p>
              <p>
                <strong>Salle :</strong> {casierDetails.salle.nom}
              </p>
              <p>
                <strong>Membre :</strong>{" "}
                {casierDetails.membre
                  ? `${casierDetails.membre.nom} ${casierDetails.membre.prenom}`
                  : "N/A"}
              </p>
              <p>
                <strong>Date Début :</strong>{" "}
                {casierDetails.dateDebut
                  ? new Date(casierDetails.dateDebut).toLocaleDateString()
                  : "N/A"}
              </p>
              <p>
                <strong>Date Fin :</strong>{" "}
                {casierDetails.dateFin
                  ? new Date(casierDetails.dateFin).toLocaleDateString()
                  : "N/A"}
              </p>
              <button
                onClick={() => {
                  setSelectedCasierId(null);
                  setCasierDetails(null);
                }}
                className="mt-4 px-4 py-2 bg-gray-300 rounded hover:bg-gray-400"
              >
                Fermer
              </button>
            </div>
          </div>
        )}
      </main>
    </div>
  );
}

export default GestionCasier;
