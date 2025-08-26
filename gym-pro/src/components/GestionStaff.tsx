import { useState, useEffect } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";

interface Staff {
  id: number;
  nom: string;
  prenom: string;
  email: string;
  telephone: string;
  adresse: string;
  genre: "HOMME" | "FEMME";
  date_de_naissance: string;
  role: string;
  date_creation: string;
  gymId?: number;
  gymsIds?: number[];
}

interface FormData {
  nomStaff: string;
  prenomStaff: string;
  emailStaff: string;
  numeroTelephoneStaff: string;
  adresseStaff: string;
  genreStaff: "HOMME" | "FEMME";
  date_de_naissanceStaff: string;
  passwordStaff: string;
  roleStaff: string;
}

interface TableauDeBordProps {
  setIsLoggedIn: (value: boolean) => void;
}

function GestionStaff({ setIsLoggedIn }: TableauDeBordProps) {
  const [staffs, setStaffs] = useState<Staff[]>([]);
  const [selectedStaff, setSelectedStaff] = useState<Staff | null>(null);
  const [editingStaff, setEditingStaff] = useState<Staff | null>(null);
  const [addingStaff, setAddingStaff] = useState<boolean>(false);
  const [formData, setFormData] = useState<FormData>({
    nomStaff: "",
    prenomStaff: "",
    emailStaff: "",
    numeroTelephoneStaff: "",
    adresseStaff: "",
    genreStaff: "HOMME",
    date_de_naissanceStaff: "",
    passwordStaff: "",
    roleStaff: "RECEPTIONNISTE",
  });
  const [loading, setLoading] = useState<boolean>(true);
  const [detailLoading, setDetailLoading] = useState<boolean>(false);
  const [updating, setUpdating] = useState<boolean>(false);
  const [adding, setAdding] = useState<boolean>(false);
  const [error, setError] = useState<string>("");
  const [success, setSuccess] = useState<string>("");

  // États pour la pagination
  const [currentPage, setCurrentPage] = useState<number>(1);
  const [staffsPerPage, setStaffsPerPage] = useState<number>(10);
  const navigate = useNavigate();

  // Votre token JWT
  const token = localStorage.getItem("token");

  const handleLogout = () => {
    localStorage.removeItem("token");
    setIsLoggedIn(false);
  };

  // Récupérer tous les membres du staff
  const fetchStaffs = async () => {
    try {
      const response = await axios.get<Staff[]>(
        "http://localhost:8080/api/users/staff",
        {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        }
      );
      setStaffs(response.data);
      setLoading(false);
    } catch (err: any) {
      setError("Erreur lors de la récupération du staff.");
      setLoading(false);
      console.error("Erreur détaillée:", err.response?.data || err.message);
    }
  };

  // Récupérer les détails d'un membre du staff par ID
  const fetchStaffDetails = async (id: number) => {
    setDetailLoading(true);
    try {
      const response = await axios.get<Staff>(
        `http://localhost:8080/api/users/profil/${id}`,
        {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        }
      );
      setSelectedStaff(response.data);
    } catch (err: any) {
      setError(
        "Erreur lors de la récupération des détails du membre du staff."
      );
      console.error("Erreur détaillée:", err.response?.data || err.message);
    } finally {
      setDetailLoading(false);
    }
  };

  // Préparer le formulaire de modification
  const prepareEditForm = (staff: Staff) => {
    setEditingStaff(staff);
    setFormData({
      nomStaff: staff.nom || "",
      prenomStaff: staff.prenom || "",
      emailStaff: staff.email || "",
      numeroTelephoneStaff: staff.telephone || "",
      adresseStaff: staff.adresse || "",
      genreStaff: staff.genre || "HOMME",
      date_de_naissanceStaff: staff.date_de_naissance || "",
      passwordStaff: "", // Mot de passe vide pour l'édition
      roleStaff: staff.role || "RECEPTIONNISTE",
    });
  };

  // Préparer le formulaire d'ajout
  const prepareAddForm = () => {
    setAddingStaff(true);
    setFormData({
      nomStaff: "",
      prenomStaff: "",
      emailStaff: "",
      numeroTelephoneStaff: "",
      adresseStaff: "",
      genreStaff: "HOMME",
      date_de_naissanceStaff: "",
      passwordStaff: "",
      roleStaff: "RECEPTIONNISTE",
    });
  };

  // Modifier un membre du staff
  const handleUpdateStaff = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!editingStaff) return;

    setUpdating(true);
    try {
      await axios.put(
        `http://localhost:8080/api/users/modifier-staff/${editingStaff.id}`,
        {
          nomStaff: formData.nomStaff,
          prenomStaff: formData.prenomStaff,
          emailStaff: formData.emailStaff,
          numeroTelephoneStaff: formData.numeroTelephoneStaff,
          adresseStaff: formData.adresseStaff,
          genreStaff: formData.genreStaff,
          date_de_naissanceStaff: formData.date_de_naissanceStaff,
          passwordStaff: formData.passwordStaff || undefined, // Envoyer seulement si modifié
          roleStaff: formData.roleStaff,
        },
        {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        }
      );

      setSuccess("Membre du staff modifié avec succès !");
      setEditingStaff(null);
      fetchStaffs();
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

  // Ajouter un nouveau membre du staff
  const handleAddStaff = async (e: React.FormEvent) => {
    e.preventDefault();

    setAdding(true);
    try {
      await axios.post(
        "http://localhost:8080/api/users/ajouter/staff",
        {
          nomStaff: formData.nomStaff,
          prenomStaff: formData.prenomStaff,
          emailStaff: formData.emailStaff,
          numeroTelephoneStaff: formData.numeroTelephoneStaff,
          adresseStaff: formData.adresseStaff,
          genreStaff: formData.genreStaff,
          date_de_naissanceStaff: formData.date_de_naissanceStaff,
          passwordStaff: formData.passwordStaff,
          roleStaff: formData.roleStaff,
        },
        {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        }
      );

      setSuccess("Membre du staff ajouté avec succès !");
      setAddingStaff(false);
      fetchStaffs();
      setTimeout(() => setSuccess(""), 3000);
    } catch (err: any) {
      setError(
        `Erreur lors de l'ajout : ${err.response?.data?.message || err.message}`
      );
    } finally {
      setAdding(false);
    }
  };

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
  ) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const closeDetails = () => {
    setSelectedStaff(null);
  };

  const closeEditForm = () => {
    setEditingStaff(null);
  };

  const closeAddForm = () => {
    setAddingStaff(false);
  };

  // Pagination : Calculer les membres du staff à afficher
  const indexOfLastStaff = currentPage * staffsPerPage;
  const indexOfFirstStaff = indexOfLastStaff - staffsPerPage;
  const currentStaffs = staffs.slice(indexOfFirstStaff, indexOfLastStaff);
  const totalPages = Math.ceil(staffs.length / staffsPerPage);

  // Pagination : Changer de page
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

  // Gérer le changement du nombre de membres du staff par page
  const handleStaffsPerPageChange = (
    e: React.ChangeEvent<HTMLSelectElement>
  ) => {
    setStaffsPerPage(Number(e.target.value));
    setCurrentPage(1); // Réinitialiser à la première page
  };

  useEffect(() => {
    fetchStaffs();
  }, []);

  if (loading) {
    return (
      <div className="min-h-screen bg-black flex items-center justify-center">
        <div className="text-white text-xl">Chargement du staff...</div>
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

        {/* Liens de navigation */}
        <nav className="flex space-x-6 font-bold font-inter">
          <button
            onClick={() => navigate("/")}
            className="hover:underline hover:text-orange-500 text-xl transition cursor-pointer text-white bg-transparent border-none"
          >
            Tableau de bord
          </button>

          {/* Menu Autres */}
          <div className="relative group">
            <button className="text-white hover:text-orange-500 text-xl transition cursor-pointer bg-transparent border-none">
              Administration
            </button>
            <div className="absolute left-0 mt-1 hidden group-hover:block w-64 bg-black border border-orange-600 rounded shadow-lg z-10 text-xs">
              <button className="block w-full text-left px-4 py-2 text-white hover:text-orange-400 cursor-pointer">
                Gestion des membres
              </button>
              <button className="block w-full text-left px-4 py-2 text-white hover:text-orange-400 cursor-pointer bg-orange-600">
                Gestion du staff
              </button>
              <button className="block w-full text-left px-4 py-2 text-white hover:text-orange-400 cursor-pointer">
                Transaction
              </button>
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

      <main className="flex-1 pt-24 p-6">
        <div className="flex justify-between items-center mb-6">
          <div>
            <h1 className="text-3xl font-bold text-white mb-2">
              Gestion du Staff
            </h1>
            <p className="text-gray-400">
              {staffs.length} membre{staffs.length !== 1 ? "s" : ""} du staff
              trouvé
              {staffs.length !== 1 ? "s" : ""} dans votre salle de sport
            </p>
          </div>
          <button
            onClick={prepareAddForm}
            className="bg-orange-500 text-white px-6 py-2 rounded-lg hover:bg-white hover:text-orange-500 transition-colors font-bold"
          >
            + Ajouter un staff
          </button>
        </div>

        {/* Messages d'alerte */}
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

        {/* Contrôles de pagination en haut */}
        {staffs.length > 0 && (
          <div className="mb-4 flex flex-col md:flex-row justify-between items-center space-y-2 md:space-y-0">
            <div className="flex items-center space-x-2">
              <span className="text-white">Staff par page:</span>
              <select
                value={staffsPerPage}
                onChange={handleStaffsPerPageChange}
                className="p-1 border rounded bg-white"
              >
                <option value="5">5</option>
                <option value="10">10</option>
                <option value="20">20</option>
                <option value="50">50</option>
              </select>
            </div>

            <div className="text-white">
              Affichage de {indexOfFirstStaff + 1} à{" "}
              {Math.min(indexOfLastStaff, staffs.length)} sur {staffs.length}{" "}
              membres du staff
            </div>
          </div>
        )}

        {/* Tableau du staff */}
        <div className="bg-white rounded-lg shadow-lg overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-orange-500 text-white">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider">
                    Nom & Prénom
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider">
                    Email
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider">
                    Téléphone
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider">
                    Genre
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
                {currentStaffs.map((staff) => (
                  <tr
                    key={staff.id}
                    className="hover:bg-gray-300 cursor-pointer transition-colors odd:bg-gray-100 even:bg-gray-200"
                    onClick={() => fetchStaffDetails(staff.id)}
                  >
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm font-medium text-gray-900">
                        {staff.nom} {staff.prenom}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm text-gray-900">{staff.email}</div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm text-gray-900">
                        {staff.telephone}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span
                        className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${
                          staff.genre === "HOMME"
                            ? "bg-blue-100 text-blue-800"
                            : "bg-pink-100 text-pink-800"
                        }`}
                      >
                        {staff.genre}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span
                        className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${
                          staff.role === "ADMIN"
                            ? "bg-red-100 text-red-800"
                            : staff.role === "GERANT"
                            ? "bg-purple-100 text-purple-800"
                            : staff.role === "COACH"
                            ? "bg-blue-100 text-blue-800"
                            : staff.role === "RECEPTIONNISTE"
                            ? "bg-green-100 text-green-800"
                            : "bg-gray-100 text-gray-800"
                        }`}
                      >
                        {staff.role}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm text-gray-900">
                        {staff.date_creation
                          ? new Date(staff.date_creation).toLocaleDateString(
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

        {/* Pagination en bas */}
        {staffs.length > 0 && (
          <div className="mt-6 flex flex-col md:flexRow justify-between items-center space-y-4 md:space-y-0">
            <div className="text-white">
              Page {currentPage} sur {totalPages}
            </div>

            <div className="flex items-center space-x-4">
              <button
                onClick={handlePreviousPage}
                disabled={currentPage === 1}
                className="bg-orange-500 text-white px-4 py-2 rounded-lg hover:bg-orange-600 transition-colors disabled:opacity-50"
              >
                Précédent
              </button>

              <div className="flex space-x-1">
                {/* Afficher les numéros de page */}
                {Array.from({ length: Math.min(5, totalPages) }, (_, i) => {
                  const pageNum =
                    currentPage <= 3
                      ? i + 1
                      : currentPage >= totalPages - 2
                      ? totalPages - 4 + i
                      : currentPage - 2 + i;

                  if (pageNum > 0 && pageNum <= totalPages) {
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
                        {pageNum}
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
                disabled={currentPage === totalPages}
                className="bg-orange-500 text-white px-4 py-2 rounded-lg hover:bg-orange-600 transition-colors disabled:opacity-50"
              >
                Suivant
              </button>
            </div>
          </div>
        )}

        {staffs.length === 0 && (
          <div className="text-center py-8 text-gray-500 bg-white rounded-lg mt-4">
            Aucun membre du staff trouvé dans la base de données.
          </div>
        )}

        {/* Bouton d'actualisation */}
        <div className="mt-6 flex justify-center">
          <button
            onClick={fetchStaffs}
            className="bg-orange-500 text-white px-6 py-2 rounded-lg hover:bg-white hover:text-orange-500 transition-colors font-bold"
          >
            Actualiser la liste
          </button>
        </div>

        {/* Modal des détails du membre du staff */}
        {selectedStaff && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div className="bg-white rounded-lg p-6 w-full max-w-2xl max-h-90vh overflow-y-auto">
              <div className="flex justify-between items-center mb-4">
                <h2 className="text-2xl font-bold">
                  Détails du Membre du Staff
                </h2>
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
                          <strong>Nom complet:</strong> {selectedStaff.nom}{" "}
                          {selectedStaff.prenom}
                        </p>
                        <p>
                          <strong>Email:</strong> {selectedStaff.email}
                        </p>
                        <p>
                          <strong>Téléphone:</strong> {selectedStaff.telephone}
                        </p>
                        <p>
                          <strong>Genre:</strong> {selectedStaff.genre}
                        </p>
                        <p>
                          <strong>Date de naissance:</strong>{" "}
                          {selectedStaff.date_de_naissance
                            ? new Date(
                                selectedStaff.date_de_naissance
                              ).toLocaleDateString("fr-FR")
                            : "Non spécifiée"}
                        </p>
                      </div>
                    </div>

                    <div>
                      <h3 className="font-semibold text-lg mb-3">
                        Informations professionnelles
                      </h3>
                      <div className="space-y-2">
                        <p>
                          <strong>Adresse:</strong>{" "}
                          {selectedStaff.adresse || "Non spécifiée"}
                        </p>
                        <p>
                          <strong>Rôle:</strong> {selectedStaff.role}
                        </p>
                        <p>
                          <strong>Date d'inscription:</strong>{" "}
                          {selectedStaff.date_creation
                            ? new Date(
                                selectedStaff.date_creation
                              ).toLocaleDateString("fr-FR")
                            : "N/A"}
                        </p>
                      </div>
                    </div>
                  </div>

                  <div className="flex justify-end space-x-3">
                    <button
                      onClick={closeDetails}
                      className="bg-black text-orange-500 px-4 py-2 rounded hover:bg-gray-600 transition-colors"
                    >
                      Fermer
                    </button>
                    <button
                      onClick={() => {
                        prepareEditForm(selectedStaff);
                        closeDetails();
                      }}
                      className="bg-orange-500 text-white px-6 py-2 rounded-lg hover:bg-orange-400 transition-colors font-bold"
                    >
                      Modifier
                    </button>
                  </div>
                </>
              )}
            </div>
          </div>
        )}

        {/* Modal de modification du membre du staff */}
        {editingStaff && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div className="bg-white rounded-lg p-6 w-full max-w-2xl max-h-90vh overflow-y-auto">
              <div className="flex justify-between items-center mb-4">
                <h2 className="text-2xl font-bold">
                  Modifier le Membre du Staff
                </h2>
                <button
                  onClick={closeEditForm}
                  className="text-gray-500 hover:text-gray-700 text-2xl"
                >
                  ×
                </button>
              </div>

              <form onSubmit={handleUpdateStaff} className="space-y-4">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-gray-700">Nom</label>
                    <input
                      type="text"
                      name="nomStaff"
                      value={formData.nomStaff}
                      onChange={handleChange}
                      className="w-full p-2 border rounded"
                      required
                    />
                  </div>
                  <div>
                    <label className="block text-gray-700">Prénom</label>
                    <input
                      type="text"
                      name="prenomStaff"
                      value={formData.prenomStaff}
                      onChange={handleChange}
                      className="w-full p-2 border rounded"
                      required
                    />
                  </div>
                  <div>
                    <label className="block text-gray-700">Email</label>
                    <input
                      type="email"
                      name="emailStaff"
                      value={formData.emailStaff}
                      onChange={handleChange}
                      className="w-full p-2 border rounded"
                      required
                    />
                  </div>
                  <div>
                    <label className="block text-gray-700">Téléphone</label>
                    <input
                      type="text"
                      name="numeroTelephoneStaff"
                      value={formData.numeroTelephoneStaff}
                      onChange={handleChange}
                      className="w-full p-2 border rounded"
                      required
                    />
                  </div>
                  <div>
                    <label className="block text-gray-700">Adresse</label>
                    <input
                      type="text"
                      name="adresseStaff"
                      value={formData.adresseStaff}
                      onChange={handleChange}
                      className="w-full p-2 border rounded"
                    />
                  </div>
                  <div>
                    <label className="block text-gray-700">Genre</label>
                    <select
                      name="genreStaff"
                      value={formData.genreStaff}
                      onChange={handleChange}
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
                      name="date_de_naissanceStaff"
                      value={formData.date_de_naissanceStaff}
                      onChange={handleChange}
                      className="w-full p-2 border rounded"
                    />
                  </div>
                  <div>
                    <label className="block text-gray-700">Rôle</label>
                    <select
                      name="roleStaff"
                      value={formData.roleStaff}
                      onChange={handleChange}
                      className="w-full p-2 border rounded"
                    >
                      <option value="RECEPTIONNISTE">Réceptionniste</option>
                      <option value="COACH">Coach</option>
                      <option value="GERANT">Gérant</option>
                      <option value="ADMIN">Administrateur</option>
                    </select>
                  </div>
                </div>

                <div className="flex justify-end space-x-3 mt-6">
                  <button
                    type="button"
                    onClick={closeEditForm}
                    className="bg-black text-orange-500 px-4 py-2 rounded hover:bg-gray-600 transition-colors"
                  >
                    Annuler
                  </button>
                  <button
                    type="submit"
                    disabled={updating}
                    className="bg-orange-500 text-white px-6 py-2 rounded-lg hover:bg-orange-400 transition-colors font-bold"
                  >
                    {updating ? "Modification..." : "Modifier"}
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}

        {/* Modal d'ajout de membre du staff */}
        {addingStaff && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div className="bg-white rounded-lg p-6 w-full max-w-2xl max-h-90vh overflow-y-auto">
              <div className="flex justify-between items-center mb-4">
                <h2 className="text-2xl font-bold">
                  Ajouter un Nouveau Membre du Staff
                </h2>
                <button
                  onClick={closeAddForm}
                  className="text-gray-500 hover:text-gray-700 text-2xl"
                >
                  ×
                </button>
              </div>

              <form onSubmit={handleAddStaff} className="space-y-4">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-gray-700">Nom *</label>
                    <input
                      type="text"
                      name="nomStaff"
                      value={formData.nomStaff}
                      onChange={handleChange}
                      className="w-full p-2 border rounded"
                      required
                    />
                  </div>
                  <div>
                    <label className="block text-gray-700">Prénom *</label>
                    <input
                      type="text"
                      name="prenomStaff"
                      value={formData.prenomStaff}
                      onChange={handleChange}
                      className="w-full p-2 border rounded"
                      required
                    />
                  </div>
                  <div>
                    <label className="block text-gray-700">Email *</label>
                    <input
                      type="email"
                      name="emailStaff"
                      value={formData.emailStaff}
                      onChange={handleChange}
                      className="w-full p-2 border rounded"
                      required
                    />
                  </div>
                  <div>
                    <label className="block text-gray-700">Téléphone *</label>
                    <input
                      type="text"
                      name="numeroTelephoneStaff"
                      value={formData.numeroTelephoneStaff}
                      onChange={handleChange}
                      className="w-full p-2 border rounded"
                      required
                    />
                  </div>
                  <div>
                    <label className="block text-gray-700">Adresse</label>
                    <input
                      type="text"
                      name="adresseStaff"
                      value={formData.adresseStaff}
                      onChange={handleChange}
                      className="w-full p-2 border rounded"
                    />
                  </div>
                  <div>
                    <label className="block text-gray-700">Genre *</label>
                    <select
                      name="genreStaff"
                      value={formData.genreStaff}
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
                      name="date_de_naissanceStaff"
                      value={formData.date_de_naissanceStaff}
                      onChange={handleChange}
                      className="w-full p-2 border rounded"
                    />
                  </div>
                  <div>
                    <label className="block text-gray-700">Rôle *</label>
                    <select
                      name="roleStaff"
                      value={formData.roleStaff}
                      onChange={handleChange}
                      className="w-full p-2 border rounded"
                      required
                    >
                      <option value="RECEPTIONNISTE">Réceptionniste</option>
                      <option value="COACH">Coach</option>
                      <option value="GERANT">Gérant</option>
                      <option value="ADMIN">Administrateur</option>
                    </select>
                  </div>
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
                    className="bg-orange-500 text-white px-6 py-2 rounded-lg hover:bg-orange-400  transition-colors font-bold"
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

export default GestionStaff;
