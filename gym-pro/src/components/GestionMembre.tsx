import { useState, useEffect } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";

interface Membre {
  id: number;
  nom: string;
  prenom: string;
  email: string;
  telephone: string;
  adresse: string;
  genre: "HOMME" | "FEMME";
  date_de_naissance: string;
  fraisInscription: number;
  role: string;
  date_creation: string;
  fraisInscriptionPayer?: boolean;
  chefFamilleId?: number;
  gymId?: number;
  gymsIds?: number[];
}

interface FormData {
  nomMembre: string;
  prenomMembre: string;
  emailMembre: string;
  numeroTelephoneMembre: string;
  adresseMembre: string;
  genreMembre: "HOMME" | "FEMME";
  getDate_de_naissanceMembre: string;
  fraisInscriptionMembre: number;
  chefFamilleId: string;
  gymId: string;
  gymsIds: number[];
  role: string;
}

interface TableauDeBordProps {
  setIsLoggedIn: (value: boolean) => void;
}

function GestionMembre({ setIsLoggedIn }: TableauDeBordProps) {
  const [membres, setMembres] = useState<Membre[]>([]);
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
    getDate_de_naissanceMembre: "",
    fraisInscriptionMembre: 0,
    chefFamilleId: "",
    gymId: "",
    gymsIds: [],
    role: "MEMBRE",
  });
  const [loading, setLoading] = useState<boolean>(true);
  const [detailLoading, setDetailLoading] = useState<boolean>(false);
  const [updating, setUpdating] = useState<boolean>(false);
  const [adding, setAdding] = useState<boolean>(false);
  const [error, setError] = useState<string>("");
  const [success, setSuccess] = useState<string>("");

  // États pour la pagination
  const [currentPage, setCurrentPage] = useState<number>(1);
  const [membersPerPage, setMembersPerPage] = useState<number>(10); // Nombre de membres par page
  const navigate = useNavigate();

  // Votre token JWT
  const token = localStorage.getItem("token");

  const handleLogout = () => {
    localStorage.removeItem("token");
    setIsLoggedIn(false);
  };

  // Récupérer tous les membres
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
      setLoading(false);
    } catch (err: any) {
      setError("Erreur lors de la récupération des membres.");
      setLoading(false);
      console.error("Erreur détaillée:", err.response?.data || err.message);
    }
  };

  // Récupérer les détails d'un membre par ID
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
      setSelectedMembre(response.data);
    } catch (err: any) {
      setError("Erreur lors de la récupération des détails du membre.");
      console.error("Erreur détaillée:", err.response?.data || err.message);
    } finally {
      setDetailLoading(false);
    }
  };

  // Préparer le formulaire de modification
  const prepareEditForm = (membre: Membre) => {
    setEditingMembre(membre);
    setFormData({
      nomMembre: membre.nom || "",
      prenomMembre: membre.prenom || "",
      emailMembre: membre.email || "",
      numeroTelephoneMembre: membre.telephone || "",
      adresseMembre: membre.adresse || "",
      genreMembre: membre.genre || "HOMME",
      getDate_de_naissanceMembre: membre.date_de_naissance || "",
      fraisInscriptionMembre: membre.fraisInscription || 0,
      chefFamilleId: membre.chefFamilleId?.toString() || "",
      gymId: membre.gymId?.toString() || "",
      gymsIds: membre.gymsIds || [],
      role: membre.role || "MEMBRE",
    });
  };

  // Préparer le formulaire d'ajout
  const prepareAddForm = () => {
    setAddingMembre(true);
    setFormData({
      nomMembre: "",
      prenomMembre: "",
      emailMembre: "",
      numeroTelephoneMembre: "",
      adresseMembre: "",
      genreMembre: "HOMME",
      getDate_de_naissanceMembre: "",
      fraisInscriptionMembre: 0,
      chefFamilleId: "",
      gymId: "",
      gymsIds: [],
      role: "MEMBRE",
    });
  };

  // Modifier un membre
  const handleUpdateMembre = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!editingMembre) return;

    setUpdating(true);
    try {
      await axios.put(
        `http://localhost:8080/api/users/modifier-membre/${editingMembre.id}`,
        {
          nomMembre: formData.nomMembre,
          prenomMembre: formData.prenomMembre,
          emailMembre: formData.emailMembre,
          numeroTelephoneMembre: formData.numeroTelephoneMembre,
          adresseMembre: formData.adresseMembre,
          genreMembre: formData.genreMembre,
          getDate_de_naissanceMembre: formData.getDate_de_naissanceMembre,
          fraisInscriptionMembre: formData.fraisInscriptionMembre,
          chefFamilleId: formData.chefFamilleId
            ? parseInt(formData.chefFamilleId)
            : null,
          gymId: formData.gymId ? parseInt(formData.gymId) : null,
          gymsIds: formData.gymsIds,
          role: formData.role,
        },
        {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
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

  // Ajouter un nouveau membre
  const handleAddMembre = async (e: React.FormEvent) => {
    e.preventDefault();

    setAdding(true);
    try {
      await axios.post(
        "http://localhost:8080/api/users/ajouter/membre",
        {
          nomMembre: formData.nomMembre,
          prenomMembre: formData.prenomMembre,
          emailMembre: formData.emailMembre,
          numeroTelephoneMembre: formData.numeroTelephoneMembre,
          adresseMembre: formData.adresseMembre,
          genreMembre: formData.genreMembre,
          getDate_de_naissanceMembre: formData.getDate_de_naissanceMembre,
          fraisInscriptionMembre: formData.fraisInscriptionMembre,
          chefFamilleId: formData.chefFamilleId
            ? parseInt(formData.chefFamilleId)
            : null,
          gymId: formData.gymId ? parseInt(formData.gymId) : null,
          gymsIds: formData.gymsIds,
          role: formData.role,
        },
        {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        }
      );

      setSuccess("Membre ajouté avec succès !");
      setAddingMembre(false);
      fetchMembres();
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

  const handleGymsChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const options = e.target.options;
    const selectedIds: number[] = [];
    for (let i = 0; i < options.length; i++) {
      if (options[i].selected) {
        selectedIds.push(parseInt(options[i].value));
      }
    }
    setFormData((prev) => ({ ...prev, gymsIds: selectedIds }));
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

  // Pagination : Calculer les membres à afficher
  const indexOfLastMember = currentPage * membersPerPage;
  const indexOfFirstMember = indexOfLastMember - membersPerPage;
  const currentMembers = membres.slice(indexOfFirstMember, indexOfLastMember);
  const totalPages = Math.ceil(membres.length / membersPerPage);

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

  // Gérer le changement du nombre de membres par page
  const handleMembersPerPageChange = (
    e: React.ChangeEvent<HTMLSelectElement>
  ) => {
    setMembersPerPage(Number(e.target.value));
    setCurrentPage(1); // Réinitialiser à la première page
  };

  useEffect(() => {
    fetchMembres();
  }, []);

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
              Affichage de {indexOfFirstMember + 1} à{" "}
              {Math.min(indexOfLastMember, membres.length)} sur {membres.length}{" "}
              membres
            </div>
          </div>
        )}

        {/* Tableau des membres */}
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
                {/* CORRECTION: Utiliser currentMembers au lieu de membres pour l'affichage paginé */}
                {currentMembers.map((membre) => (
                  <tr
                    key={membre.id}
                    className="hover:bg-gray-300 cursor-pointer transition-colors odd:bg-gray-100 even:bg-gray-200"
                    onClick={() => fetchMembreDetails(membre.id)}
                  >
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm font-medium text-gray-900">
                        {membre.nom} {membre.prenom}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm text-gray-900">
                        {membre.email}
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

        {/* Pagination en bas */}
        {membres.length > 0 && (
          <div className="mt-6 flex flex-col md:flex-row justify-between items-center space-y-4 md:space-y-0">
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

        {membres.length === 0 && (
          <div className="text-center py-8 text-gray-500 bg-white rounded-lg mt-4">
            Aucun membre trouvé dans la base de données.
          </div>
        )}

        {/* Bouton d'actualisation */}
        <div className="mt-6 flex justify-center">
          <button
            onClick={fetchMembres}
            className="bg-orange-500 text-white px-6 py-2 rounded-lg hover:bg-orange-600 transition-colors"
          >
            Actualiser la liste
          </button>
        </div>

        {/* Modal des détails du membre */}
        {selectedMembre && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
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

        {/* Modal de modification du membre */}
        {editingMembre && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
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
                      name="getDate_de_naissanceMembre"
                      value={formData.getDate_de_naissanceMembre}
                      onChange={handleChange}
                      className="w-full p-2 border rounded"
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
                      className="w-full p-2 border rounded"
                      required
                    />
                  </div>
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
                    disabled={updating}
                    className="bg-orange-500 text-white px-4 py-2 rounded hover:bg-orange-600 transition-colors disabled:opacity-50"
                  >
                    {updating ? "Modification..." : "Modifier"}
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}

        {/* Modal d'ajout de membre */}
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
                      name="getDate_de_naissanceMembre"
                      value={formData.getDate_de_naissanceMembre}
                      onChange={handleChange}
                      className="w-full p-2 border rounded"
                    />
                  </div>
                  <div>
                    <label className="block text-gray-700">
                      Frais d'inscription *
                    </label>
                    <input
                      type="number"
                      name="fraisInscriptionMembre"
                      value={formData.fraisInscriptionMembre}
                      onChange={handleChange}
                      step="0.01"
                      className="w-full p-2 border rounded"
                      required
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
                    disabled={adding}
                    className="bg-green-500 text-white px-4 py-2 rounded hover:bg-green-600 transition-colors disabled:opacity-50"
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
