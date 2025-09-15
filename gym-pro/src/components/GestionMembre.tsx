import { useState, useEffect } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";

interface TypeDeService {
  id: number;
  nom: string;
  description: string;
  tarifHomme: number;
  tarifFemme: number;
  tarifUnique: number;
  fraisInscription: number;
  gym: {
    id: number;
    nom: string;
    adresse: string;
    email: string;
    telephone: string;
  };
}

interface Membre {
  id: number;
  nom: string;
  prenom: string;
  email: string;
  telephone: string;
  adresse: string;
  genre: "HOMME" | "FEMME";
  typeDeService: TypeDeService[] | null;
  date_de_naissance: string;
  fraisInscription: number;
  role: string;
  date_creation: string;
  fraisInscriptionPayer?: boolean;
  chefFamilleId?: number;
  gymId?: number;
  gymsIds?: number[];
  profil?: string | null | undefined; // Ajout pour stocker l'image en base64 (optionnel)
}

interface FormData {
  nomMembre: string;
  prenomMembre: string;
  emailMembre: string;
  numeroTelephoneMembre: string;
  adresseMembre: string;
  genreMembre: "HOMME" | "FEMME";
  date_de_naissance: string;
  fraisInscriptionMembre: number;
  chefFamilleId: string;
  gymId: string;
  gymsIds: number[];
  role: string;
  typeDeService?: number;
  photo?: File | null; // Ajout pour gérer le téléversement d'image
}

interface PaginationResponse {
  content: Membre[];
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
}

interface TableauDeBordProps {
  setIsLoggedIn: (value: boolean) => void;
}

function GestionMembre({ setIsLoggedIn }: TableauDeBordProps) {
  const [membres, setMembres] = useState<Membre[]>([]);
  const [typeDeService, setTypesService] = useState<TypeDeService[]>([]);
  const [selectedMembre, setSelectedMembre] = useState<Membre | null>(null);
  const [editingMembre, setEditingMembre] = useState<Membre | null>(null);
  const [addingMembre, setAddingMembre] = useState<boolean>(false);
  const [formData, setFormData] = useState<FormData>({
    nomMembre: "",
    prenomMembre: "",
    emailMembre: "",
    numeroTelephoneMembre: "",
    adresseMembre: "",
    genreMembre: "HOMME",
    date_de_naissance: "",
    fraisInscriptionMembre: 0,
    chefFamilleId: "",
    gymId: "",
    gymsIds: [],
    role: "MEMBRE",
    typeDeService: undefined,
    photo: null,
  });
  const [loading, setLoading] = useState<boolean>(true);
  const [detailLoading, setDetailLoading] = useState<boolean>(false);
  const [updating, setUpdating] = useState<boolean>(false);
  const [adding, setAdding] = useState<boolean>(false);
  const [error, setError] = useState<string>("");
  const [success, setSuccess] = useState<string>("");

  const [currentPage, setCurrentPage] = useState<number>(0);
  const [membersPerPage, setMembersPerPage] = useState<number>(10);
  const [totalPages, setTotalPages] = useState<number>(0);
  const navigate = useNavigate();

  const [membreSearch, setMembreSearch] = useState<string>("");
  const [filteredMembres, setFilteredMembres] = useState<Membre[]>([]);
  const [query, setQuery] = useState<string>("");
  const [filterTypeService, setFilterTypeService] = useState<string>("");

  const token = localStorage.getItem("token");

  const handleLogout = () => {
    localStorage.removeItem("token");
    setIsLoggedIn(false);
  };

  const calculateAge = (birthDate: string) => {
    if (!birthDate) return null;
    const birth = new Date(birthDate);
    const today = new Date();
    let age = today.getFullYear() - birth.getFullYear();
    const monthDiff = today.getMonth() - birth.getMonth();
    if (
      monthDiff < 0 ||
      (monthDiff === 0 && today.getDate() < birth.getDate())
    ) {
      age--;
    }
    return age;
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

  const fetchMembres = async () => {
    try {
      const response = await axios.get<PaginationResponse>(
        `http://localhost:8080/api/users/membre/paginer?page=${currentPage}&size=${membersPerPage}`,
        {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        }
      );
      const membresData = response.data.content.map((membre: any) => ({
        ...membre,
        typeDeService: Array.isArray(membre.typeDeService)
          ? membre.typeDeService
          : membre.typeDeService
          ? [membre.typeDeService]
          : [],
        profil: membre.profil
          ? `data:image/jpeg;base64,${membre.profil}`
          : null, // Conversion en base64 pour affichage
      }));
      setMembres(membresData);
      setTotalPages(response.data.totalPages);
      setLoading(false);
    } catch (err: any) {
      setError("Erreur lors de la récupération des membres.");
      setLoading(false);
      console.error("Erreur détaillée:", err.response?.data || err.message);
    }
  };

  const fetchMembreDetails = async (id: number) => {
    setDetailLoading(true);
    try {
      const response = await axios.get<Membre>(
        `http://localhost:8080/api/users/profil/${id}`,
        {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        }
      );
      setSelectedMembre({
        ...response.data,
        profil: response.data.profil
          ? `data:image/jpeg;base64,${response.data.profil}`
          : null,
      });
    } catch (err: any) {
      setError("Erreur lors de la récupération des détails du membre.");
      console.error("Erreur détaillée:", err.response?.data || err.message);
    } finally {
      setDetailLoading(false);
    }
  };

  const prepareEditForm = (membre: Membre) => {
    setEditingMembre(membre);
    setFormData({
      nomMembre: membre.nom || "",
      prenomMembre: membre.prenom || "",
      emailMembre: membre.email || "",
      numeroTelephoneMembre: membre.telephone || "",
      adresseMembre: membre.adresse || "",
      genreMembre: membre.genre || "HOMME",
      date_de_naissance: membre.date_de_naissance || "",
      fraisInscriptionMembre: membre.fraisInscription || 0,
      chefFamilleId: membre.chefFamilleId?.toString() || "",
      gymId: membre.gymId?.toString() || "",
      gymsIds: membre.gymsIds || [],
      role: membre.role || "MEMBRE",
      typeDeService:
        membre.typeDeService && membre.typeDeService.length > 0
          ? membre.typeDeService[0].id
          : undefined,
      photo: null, // Réinitialiser la photo pour modification
    });
  };

  const prepareAddForm = () => {
    setAddingMembre(true);
    setFormData({
      nomMembre: "",
      prenomMembre: "",
      emailMembre: "",
      numeroTelephoneMembre: "",
      adresseMembre: "",
      genreMembre: "HOMME",
      date_de_naissance: "",
      fraisInscriptionMembre: 0,
      chefFamilleId: "",
      gymId: "",
      gymsIds: [],
      role: "MEMBRE",
      typeDeService: undefined,
      photo: null,
    });
  };

  const handleTypeDeServiceChange = (
    e: React.ChangeEvent<HTMLSelectElement>
  ) => {
    const typeId = parseInt(e.target.value);
    const selectedType = typeDeService.find((type) => type.id === typeId);
    if (selectedType) {
      const frais =
        formData.genreMembre === "HOMME"
          ? selectedType.tarifHomme
          : selectedType.tarifFemme || selectedType.tarifUnique;
      setFormData((prev) => ({
        ...prev,
        typeDeService: typeId,
        fraisInscriptionMembre: frais,
      }));
    }
  };

  const handleUpdateMembre = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!editingMembre) return;

    setUpdating(true);
    const formDataToSend = new FormData();
    formDataToSend.append("nomMembre", formData.nomMembre);
    formDataToSend.append("prenomMembre", formData.prenomMembre);
    formDataToSend.append("emailMembre", formData.emailMembre);
    formDataToSend.append(
      "numeroTelephoneMembre",
      formData.numeroTelephoneMembre
    );
    formDataToSend.append("adresseMembre", formData.adresseMembre);
    formDataToSend.append("genreMembre", formData.genreMembre);
    formDataToSend.append(
      "date_de_naissanceMembre",
      formData.date_de_naissance
    );
    if (formData.typeDeService)
      formDataToSend.append("typeDeService", formData.typeDeService.toString());
    if (formData.chefFamilleId)
      formDataToSend.append("chefFamilleId", formData.chefFamilleId);
    if (formData.gymId) formDataToSend.append("gymId", formData.gymId);
    formData.gymsIds.forEach((id) =>
      formDataToSend.append("gymsIds", id.toString())
    );
    formDataToSend.append("role", formData.role);
    if (formData.photo) formDataToSend.append("file", formData.photo);

    try {
      await axios.put(
        `http://localhost:8080/api/users/modifier-membre/${editingMembre.id}`,
        formDataToSend,
        {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "multipart/form-data",
          },
        }
      );

      setSuccess("Membre modifié avec succès !");
      setEditingMembre(null);
      fetchMembres();
      setTimeout(() => setSuccess(""), 3000);
    } catch (err: any) {
      setError(
        `Erreur lors de la modification : ${
          err.response?.data?.message || err.message
        }`
      );
    } finally {
      setUpdating(false);
    }
  };

  const handleAddMembre = async (e: React.FormEvent) => {
    e.preventDefault();

    if (
      !formData.nomMembre.trim() ||
      !formData.prenomMembre.trim() ||
      !formData.emailMembre.trim() ||
      !formData.numeroTelephoneMembre.trim()
    ) {
      setError("Tous les champs obligatoires doivent être remplis");
      return;
    }

    const age = calculateAge(formData.date_de_naissance);
    if (age === null || age < 16 || age > 80) {
      setError("L'âge doit être compris entre 16 et 80 ans.");
      return;
    }

    if (!formData.typeDeService) {
      setError("Veuillez sélectionner un type de service.");
      return;
    }

    setAdding(true);
    const formDataToSend = new FormData();
    formDataToSend.append("nomMembre", formData.nomMembre);
    formDataToSend.append("prenomMembre", formData.prenomMembre);
    formDataToSend.append("emailMembre", formData.emailMembre);
    formDataToSend.append(
      "numeroTelephoneMembre",
      formData.numeroTelephoneMembre
    );
    formDataToSend.append("adresseMembre", formData.adresseMembre);
    formDataToSend.append("genreMembre", formData.genreMembre);
    formDataToSend.append(
      "date_de_naissanceMembre",
      formData.date_de_naissance
    );
    if (formData.typeDeService)
      formDataToSend.append("typeDeService", formData.typeDeService.toString());
    if (formData.photo) formDataToSend.append("file", formData.photo);

    try {
      const response = await axios.post(
        "http://localhost:8080/api/users/ajouter/membre",
        formDataToSend,
        {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "multipart/form-data",
          },
        }
      );

      setSuccess("Membre ajouté avec succès !");
      setAddingMembre(false);
      fetchMembres();
      setTimeout(() => setSuccess(""), 3000);
    } catch (err: any) {
      console.error("Erreur détaillée:", err.response?.data);
      setError(
        `Erreur lors de l'ajout : ${
          err.response?.data?.message ||
          err.response?.data?.error ||
          err.message
        }`
      );
    } finally {
      setAdding(false);
    }
  };

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
  ) => {
    const { name, value, files } = e.target as HTMLInputElement;
    if (name === "photo" && files) {
      setFormData((prev) => ({
        ...prev,
        [name]: files[0],
      }));
    } else if (name === "typeDeService") {
      const numericValue = value ? parseInt(value) : undefined;
      setFormData((prev) => ({ ...prev, [name]: numericValue }));
    } else {
      setFormData((prev) => ({ ...prev, [name]: value }));
    }
  };

  const closeDetails = () => {
    setSelectedMembre(null);
  };

  const closeEditForm = () => {
    setEditingMembre(null);
  };

  const closeAddForm = () => {
    setAddingMembre(false);
  };

  const handlePreviousPage = () => {
    if (currentPage > 0) {
      setCurrentPage(currentPage - 1);
    }
  };

  const handleNextPage = () => {
    if (currentPage < totalPages - 1) {
      setCurrentPage(currentPage + 1);
    }
  };

  const handleMembersPerPageChange = (
    e: React.ChangeEvent<HTMLSelectElement>
  ) => {
    setMembersPerPage(Number(e.target.value));
    setCurrentPage(0);
  };

  const handleSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setQuery(e.target.value);
  };

  const handleFilterTypeServiceChange = (
    e: React.ChangeEvent<HTMLSelectElement>
  ) => {
    setFilterTypeService(e.target.value);
  };

  const filteredMembre = membres.filter((membre) => {
    const matchesQuery =
      !query ||
      `${membre.nom} ${membre.prenom} ${membre.email} ${membre.telephone} ${membre.adresse} ${membre.role}`
        .toLowerCase()
        .includes(query.toLowerCase());

    const matchesFilterTypeService =
      !filterTypeService ||
      (membre.typeDeService &&
        membre.typeDeService.some(
          (service) => service.nom === filterTypeService
        ));

    return matchesQuery && matchesFilterTypeService;
  });

  useEffect(() => {
    fetchMembres();
    fetchTypesService();
  }, [currentPage, membersPerPage]);

  if (loading) {
    return (
      <div className="min-h-screen bg-black flex items-center justify-center">
        <div className="text-white text-xl">Chargement des membres...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-black flex items-center justify-center">
        <div className="text-red-500 text-xl">{error}</div>
        <button
          onClick={() => window.location.reload()}
          className="ml-4 bg-orange-500 text-white px-4 py-2 rounded"
        >
          Réessayer
        </button>
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
              <button className="block w-full text-left px-4 py-2 text-white hover:text-orange-400 cursor-pointer bg-orange-600">
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

      <main className="flex-1 pt-24 p-6">
        <div className="flex justify-between items-center mb-6">
          <div>
            <h1 className="text-3xl font-bold text-white mb-2">
              Gestion des Membres
            </h1>
            <p className="text-gray-400">
              {membres.length} membre{membres.length !== 1 ? "s" : ""} trouvé
              {membres.length !== 1 ? "s" : ""} dans votre salle de sport
            </p>
          </div>
          <button
            onClick={prepareAddForm}
            className="bg-orange-500 text-white px-6 py-2 rounded-lg hover:bg-green-600 transition-colors font-bold"
          >
            + Ajouter un membre
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

        {membres.length > 0 && (
          <div className="mb-4 flex flex-col md:flex-row justify-between items-center space-y-2 md:space-y-0">
            <div className="flex items-center space-x-2">
              <span className="text-white">Membres par page:</span>
              <select
                value={membersPerPage}
                onChange={handleMembersPerPageChange}
                className="p-1 border rounded bg-white"
              >
                <option value="5">5</option>
                <option value="10">10</option>
                <option value="20">20</option>
                <option value="50">50</option>
              </select>
            </div>

            <div className="text-white">
              Affichage de {currentPage * membersPerPage + 1} à{" "}
              {Math.min(
                (currentPage + 1) * membersPerPage,
                totalPages * membersPerPage
              )}{" "}
              sur {totalPages * membersPerPage} membres
            </div>
          </div>
        )}

        {/* Filtres */}
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
              {typeDeService.map((service) => (
                <option key={service.id} value={service.nom}>
                  {service.nom}
                </option>
              ))}
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
                    Photo
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider">
                    Nom & Prénom
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider">
                    Type de service
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider">
                    Téléphone
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider">
                    Genre
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider">
                    Frais
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider">
                    Rôle
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider">
                    Date inscription
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {filteredMembre.map((membre) => (
                  <tr
                    key={membre.id}
                    className="hover:bg-gray-300 cursor-pointer transition-colors odd:bg-gray-100 even:bg-gray-200"
                    onClick={() => fetchMembreDetails(membre.id)}
                  >
                    <td className="px-6 py-4 whitespace-nowrap">
                      <img
                        src={membre.profil || "/src/assets/person-96.png"}
                        alt="Profil"
                        className="w-12 h-12 object-cover rounded-full"
                      />
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm font-medium text-gray-900">
                        {membre.nom} {membre.prenom}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm text-gray-900">
                        {membre.typeDeService && membre.typeDeService.length > 0
                          ? membre.typeDeService[0]?.nom || "Non défini"
                          : "Non défini"}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm text-gray-900">
                        {membre.telephone}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span
                        className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${
                          membre.genre === "HOMME"
                            ? "bg-blue-100 text-blue-800"
                            : "bg-pink-100 text-pink-800"
                        }`}
                      >
                        {membre.genre}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm text-gray-900">
                        {membre.fraisInscription} FCFA
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span
                        className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${
                          membre.role === "ADMIN"
                            ? "bg-red-100 text-red-800"
                            : membre.role === "GERANT"
                            ? "bg-purple-100 text-purple-800"
                            : membre.role === "MEMBRE"
                            ? "bg-green-100 text-green-800"
                            : "bg-gray-100 text-gray-800"
                        }`}
                      >
                        {membre.role}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm text-gray-900">
                        {membre.date_creation
                          ? new Date(membre.date_creation).toLocaleDateString(
                              "fr-FR"
                            )
                          : "N/A"}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        {/* Pagination */}
        {membres.length > 0 && (
          <div className="mt-6 flex flex-col md:flex-row justify-between items-center space-y-4 md:space-y-0">
            <div className="text-white">
              Page {currentPage + 1} sur {totalPages}
            </div>

            <div className="flex items-center space-x-4">
              <button
                onClick={handlePreviousPage}
                disabled={currentPage === 0}
                className="bg-orange-500 text-white px-4 py-2 rounded-lg hover:bg-orange-600 transition-colors disabled:opacity-50"
              >
                Précédent
              </button>

              <div className="flex space-x-1">
                {Array.from({ length: Math.min(5, totalPages) }, (_, i) => {
                  const pageNum =
                    currentPage <= 2
                      ? i
                      : currentPage >= totalPages - 3
                      ? totalPages - 5 + i
                      : currentPage - 2 + i;

                  if (pageNum >= 0 && pageNum < totalPages) {
                    return (
                      <button
                        key={pageNum}
                        onClick={() => setCurrentPage(pageNum)}
                        className={`px-3 py-1 rounded ${
                          currentPage === pageNum
                            ? "bg-orange-500 text-white"
                            : "bg-gray-200 text-gray-700 hover:bg-gray-300"
                        }`}
                      >
                        {pageNum + 1}
                      </button>
                    );
                  }
                  return null;
                })}

                {totalPages > 5 && (
                  <span className="px-2 py-1 text-white">...</span>
                )}
              </div>

              <button
                onClick={handleNextPage}
                disabled={currentPage === totalPages - 1}
                className="bg-orange-500 text-white px-4 py-2 rounded-lg hover:bg-orange-600 transition-colors disabled:opacity-50"
              >
                Suivant
              </button>
            </div>
          </div>
        )}

        {membres.length === 0 && (
          <div className="text-center py-8 text-gray-500 bg-white rounded-lg mt-4">
            Aucun membre trouvé dans la base de données.
          </div>
        )}

        <div className="mt-6 flex justify-center">
          <button
            onClick={fetchMembres}
            className="bg-orange-500 text-white px-6 py-2 rounded-lg hover:bg-orange-600 transition-colors"
          >
            Actualiser la liste
          </button>
        </div>

        {selectedMembre && (
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
                <h2 className="text-2xl font-bold">Détails du Membre</h2>
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
                      <h3 className="font-semibold text-lg mb-3">
                        Informations personnelles
                      </h3>
                      <div className="space-y-2">
                        <p>
                          <strong>Nom complet:</strong> {selectedMembre.nom}{" "}
                          {selectedMembre.prenom}
                        </p>
                        <p>
                          <strong>Email:</strong> {selectedMembre.email}
                        </p>
                        <p>
                          <strong>Type de service:</strong>{" "}
                          {selectedMembre.typeDeService &&
                          selectedMembre.typeDeService.length > 0
                            ? selectedMembre.typeDeService[0]?.nom ||
                              "Non défini"
                            : "Non défini"}
                        </p>
                        <p>
                          <strong>Téléphone:</strong> {selectedMembre.telephone}
                        </p>
                        <p>
                          <strong>Genre:</strong> {selectedMembre.genre}
                        </p>
                        <p>
                          <strong>Date de naissance:</strong>{" "}
                          {selectedMembre.date_de_naissance
                            ? new Date(
                                selectedMembre.date_de_naissance
                              ).toLocaleDateString("fr-FR")
                            : "Non spécifiée"}
                        </p>
                      </div>
                    </div>

                    <div>
                      <h3 className="font-semibold text-lg mb-3">
                        Informations supplémentaires
                      </h3>
                      <div className="space-y-2">
                        <p>
                          <strong>Adresse:</strong>{" "}
                          {selectedMembre.adresse || "Non spécifiée"}
                        </p>
                        <p>
                          <strong>Rôle:</strong> {selectedMembre.role}
                        </p>
                        <p>
                          <strong>Frais d'inscription:</strong>{" "}
                          {selectedMembre.fraisInscription} FCFA
                        </p>
                        <p>
                          <strong>Frais payés:</strong>{" "}
                          {selectedMembre.fraisInscriptionPayer ? "Oui" : "Non"}
                        </p>
                        <p>
                          <strong>Date d'inscription:</strong>{" "}
                          {selectedMembre.date_creation
                            ? new Date(
                                selectedMembre.date_creation
                              ).toLocaleDateString("fr-FR")
                            : "N/A"}
                        </p>
                        {selectedMembre.chefFamilleId && (
                          <p>
                            <strong>ID Chef de famille:</strong>{" "}
                            {selectedMembre.chefFamilleId}
                          </p>
                        )}
                        {selectedMembre.gymId && (
                          <p>
                            <strong>ID Gym principal:</strong>{" "}
                            {selectedMembre.gymId}
                          </p>
                        )}
                      </div>
                    </div>
                  </div>
                  {selectedMembre.profil && (
                    <div className="mb-4">
                      <strong>Photo de profil:</strong>
                      <img
                        src={selectedMembre.profil}
                        alt="Photo de profil"
                        className="w-32 h-32 object-cover rounded-full mt-2"
                      />
                    </div>
                  )}

                  <div className="flex justify-end space-x-3">
                    <button
                      onClick={() => {
                        prepareEditForm(selectedMembre);
                        closeDetails();
                      }}
                      className="bg-orange-500 text-white px-4 py-2 rounded hover:bg-orange-600 transition-colors"
                    >
                      Modifier
                    </button>
                    <button
                      onClick={closeDetails}
                      className="bg-gray-500 text-white px-4 py-2 rounded hover:bg-gray-600 transition-colors"
                    >
                      Fermer
                    </button>
                  </div>
                </>
              )}
            </div>
          </div>
        )}

        {editingMembre && (
          <div
            className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
            onClick={(e) => {
              const modal = e.currentTarget.querySelector(".bg-white");
              if (modal && !modal.contains(e.target as Node)) {
                closeEditForm();
              }
            }}
          >
            <div className="bg-white rounded-lg p-6 w-full max-w-2xl max-h-90vh overflow-y-auto">
              <div className="flex justify-between items-center mb-4">
                <h2 className="text-2xl font-bold">Modifier le Membre</h2>
                <button
                  onClick={closeEditForm}
                  className="text-gray-500 hover:text-gray-700 text-2xl"
                >
                  ×
                </button>
              </div>

              <form onSubmit={handleUpdateMembre} className="space-y-4">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-gray-700">Nom</label>
                    <input
                      type="text"
                      name="nomMembre"
                      value={formData.nomMembre}
                      onChange={handleChange}
                      className="w-full p-2 border rounded"
                      required
                    />
                  </div>
                  <div>
                    <label className="block text-gray-700">Prénom</label>
                    <input
                      type="text"
                      name="prenomMembre"
                      value={formData.prenomMembre}
                      onChange={handleChange}
                      className="w-full p-2 border rounded"
                      required
                    />
                  </div>
                  <div>
                    <label className="block text-gray-700">Email</label>
                    <input
                      type="email"
                      name="emailMembre"
                      value={formData.emailMembre}
                      onChange={handleChange}
                      className="w-full p-2 border rounded"
                      required
                    />
                  </div>
                  <div>
                    <label className="block text-gray-700">Téléphone</label>
                    <input
                      type="text"
                      name="numeroTelephoneMembre"
                      value={formData.numeroTelephoneMembre}
                      onChange={handleChange}
                      className="w-full p-2 border rounded"
                      required
                    />
                  </div>
                  <div>
                    <label className="block text-gray-700">Adresse</label>
                    <input
                      type="text"
                      name="adresseMembre"
                      value={formData.adresseMembre}
                      onChange={handleChange}
                      className="w-full p-2 border rounded"
                    />
                  </div>
                  <div>
                    <label className="block text-gray-700">Genre</label>
                    <select
                      name="genreMembre"
                      value={formData.genreMembre}
                      onChange={(e) => {
                        handleChange(e);
                        handleTypeDeServiceChange({
                          target: {
                            value: formData.typeDeService?.toString() || "",
                          },
                        } as React.ChangeEvent<HTMLSelectElement>);
                      }}
                      className="w-full p-2 border rounded"
                    >
                      <option value="HOMME">Homme</option>
                      <option value="FEMME">Femme</option>
                    </select>
                  </div>
                  <div>
                    <label className="block text-gray-700">
                      Date de naissance
                    </label>
                    <input
                      type="date"
                      name="date_de_naissance"
                      value={formData.date_de_naissance}
                      onChange={handleChange}
                      className="w-full p-2 border rounded"
                    />
                  </div>
                  <div>
                    <label className="block text-gray-700">Photo</label>
                    <input
                      type="file"
                      name="photo"
                      accept="image/*"
                      onChange={handleChange}
                      className="w-full p-2 border rounded"
                    />
                  </div>
                </div>
                <div>
                  <label className="block text-gray-700">Type de service</label>
                  <select
                    name="typeDeService"
                    value={formData.typeDeService?.toString() || ""}
                    onChange={(e) => {
                      const typeId = parseInt(e.target.value);
                      const selectedType = typeDeService.find(
                        (type) => type.id === typeId
                      );
                      setFormData((prev) => ({
                        ...prev,
                        typeDeService: typeId,
                        fraisInscriptionMembre: selectedType
                          ? selectedType.fraisInscription
                          : 0,
                      }));
                    }}
                    className="w-full p-2 border rounded"
                    required
                  >
                    <option value="">Sélectionner un type de service</option>
                    {typeDeService.map((type) => (
                      <option key={type.id} value={type.id}>
                        {type.nom}
                      </option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="block text-gray-700">
                    Frais d'inscription
                  </label>
                  <input
                    type="number"
                    name="fraisInscriptionMembre"
                    value={formData.fraisInscriptionMembre}
                    onChange={handleChange}
                    step="0.01"
                    className="w-full p-2 border rounded bg-gray-100"
                    readOnly
                  />
                </div>

                <div className="flex justify-end space-x-3 mt-6">
                  <button
                    type="button"
                    onClick={closeEditForm}
                    className="bg-gray-500 text-white px-4 py-2 rounded hover:bg-gray-600 transition-colors"
                  >
                    Annuler
                  </button>
                  <button
                    type="submit"
                    disabled={updating || !formData.typeDeService}
                    className="bg-orange-500 text-white px-6 py-2 rounded-lg hover:bg-orange-400 transition-colors font-bold"
                  >
                    {updating ? "Modification..." : "Modifier"}
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}

        {addingMembre && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div className="bg-white rounded-lg p-6 w-full max-w-2xl max-h-90vh overflow-y-auto">
              <div className="flex justify-between items-center mb-4">
                <h2 className="text-2xl font-bold">
                  Ajouter un Nouveau Membre
                </h2>
                <button
                  onClick={closeAddForm}
                  className="text-gray-500 hover:text-gray-700 text-2xl"
                >
                  ×
                </button>
              </div>

              <form onSubmit={handleAddMembre} className="space-y-4">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-gray-700">Nom *</label>
                    <input
                      type="text"
                      name="nomMembre"
                      value={formData.nomMembre}
                      onChange={handleChange}
                      className="w-full p-2 border rounded"
                      required
                    />
                  </div>
                  <div>
                    <label className="block text-gray-700">Prénom *</label>
                    <input
                      type="text"
                      name="prenomMembre"
                      value={formData.prenomMembre}
                      onChange={handleChange}
                      className="w-full p-2 border rounded"
                      required
                    />
                  </div>
                  <div>
                    <label className="block text-gray-700">Email *</label>
                    <input
                      type="email"
                      name="emailMembre"
                      value={formData.emailMembre}
                      onChange={handleChange}
                      className="w-full p-2 border rounded"
                      required
                    />
                  </div>
                  <div>
                    <label className="block text-gray-700">Téléphone *</label>
                    <input
                      type="text"
                      name="numeroTelephoneMembre"
                      value={formData.numeroTelephoneMembre}
                      onChange={handleChange}
                      className="w-full p-2 border rounded"
                      required
                    />
                  </div>
                  <div>
                    <label className="block text-gray-700">Adresse</label>
                    <input
                      type="text"
                      name="adresseMembre"
                      value={formData.adresseMembre}
                      onChange={handleChange}
                      className="w-full p-2 border rounded"
                    />
                  </div>
                  <div>
                    <label className="block text-gray-700">Genre *</label>
                    <select
                      name="genreMembre"
                      value={formData.genreMembre}
                      onChange={handleChange}
                      className="w-full p-2 border rounded"
                      required
                    >
                      <option value="HOMME">Homme</option>
                      <option value="FEMME">Femme</option>
                    </select>
                  </div>
                  <div>
                    <label className="block text-gray-700">
                      Date de naissance
                    </label>
                    <input
                      type="date"
                      name="date_de_naissance"
                      value={formData.date_de_naissance}
                      onChange={handleChange}
                      className="w-full p-2 border rounded"
                    />
                  </div>
                  <div>
                    <label className="block text-gray-700">
                      Type de service *
                    </label>
                    <select
                      name="typeDeService"
                      value={formData.typeDeService?.toString() || ""}
                      onChange={handleChange}
                      className="w-full p-2 border rounded"
                      required
                    >
                      <option value="">Sélectionner un type de service</option>
                      {typeDeService.map((type) => (
                        <option key={type.id} value={type.id}>
                          {type.nom}
                        </option>
                      ))}
                    </select>
                  </div>
                  <div>
                    <label className="block text-gray-700">Photo *</label>
                    <input
                      type="file"
                      name="photo"
                      accept="image/*"
                      onChange={handleChange}
                      className="w-full p-2 border rounded"
                      required
                    />
                  </div>
                  <div>
                    <label className="block text-gray-700">
                      Frais d'inscription
                    </label>
                    <input
                      type="number"
                      name="fraisInscriptionMembre"
                      value={formData.fraisInscriptionMembre}
                      onChange={handleChange}
                      step="0.01"
                      className="w-full p-2 border rounded bg-gray-100"
                      readOnly
                    />
                  </div>
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
                    disabled={
                      adding || !formData.typeDeService || !formData.photo
                    }
                    className="bg-orange-500 text-white px-4 py-2 rounded hover:bg-orange-600 transition-colors "
                  >
                    {adding ? "Ajout en cours..." : "Ajouter"}
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

export default GestionMembre;
