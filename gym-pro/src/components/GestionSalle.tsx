import React, { useState, useEffect } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";

// Composant Modal de confirmation pour la suppression
const ConfirmModal = ({
  isOpen,
  onClose,
  onConfirm,
  message,
}: {
  isOpen: boolean;
  onClose: () => void;
  onConfirm: () => void;
  message: string;
}) => {
  if (!isOpen) return null;

  return (
    <div
      className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
      onClick={(e) => {
        const modal = e.currentTarget.querySelector(".bg-white");
        if (modal && !modal.contains(e.target as Node)) {
          onClose();
        }
      }}
    >
      <div className="bg-white rounded-lg p-6 w-full max-w-md">
        <h3 className="text-xl font-semibold mb-4">Confirmation</h3>
        <p className="text-gray-700 mb-6">{message}</p>
        <div className="flex justify-end space-x-3">
          <button
            onClick={onClose}
            className="bg-black text-orange-500 px-4 py-2 rounded hover:bg-gray-600 transition-colors"
          >
            Annuler
          </button>
          <button
            onClick={onConfirm}
            className="bg-orange-500 text-white px-6 py-2 rounded-lg hover:bg-orange-400 transition-colors font-bold"
          >
            Confirmer
          </button>
        </div>
      </div>
    </div>
  );
};

interface Salle {
  id: number;
  nom: string;
}

interface FormData {
  nom: string;
}

interface TableauDeBordProps {
  setIsLoggedIn: (value: boolean) => void;
}

function GestionSalle({ setIsLoggedIn }: TableauDeBordProps) {
  const [salles, setSalles] = useState<Salle[]>([]);
  const [selectedSalle, setSelectedSalle] = useState<Salle | null>(null);
  const [editingSalle, setEditingSalle] = useState<Salle | null>(null);
  const [addingSalle, setAddingSalle] = useState<boolean>(false);
  const [formData, setFormData] = useState<FormData>({
    nom: "",
  });

  const [loading, setLoading] = useState<boolean>(false);
  const [detailLoading, setDetailLoading] = useState<boolean>(false);
  const [updating, setUpdating] = useState<boolean>(false);
  const [adding, setAdding] = useState<boolean>(false);
  const [error, setError] = useState<string>("");
  const [success, setSuccess] = useState<string>("");

  // Etat pour la pagination
  const [currentPage, setCurrentPage] = useState<number>(1);
  const [sallesPerPage, setSallesPerPage] = useState<number>(10);
  const [confirmModal, setConfirmModal] = useState<{
    isOpen: boolean;
    id: number | null;
    message: string;
  }>({
    isOpen: false,
    id: null,
    message: "",
  });
  const navigate = useNavigate();

  // Mon token JWT
  const token = localStorage.getItem("token");

  const handleLogout = () => {
    localStorage.removeItem("token");
    setIsLoggedIn(false);
  };

  // Récuperer tous les salles du gym
  const fetchSalles = async () => {
    try {
      const response = await axios.get<Salle[]>(
        "http://localhost:8080/api/salles/gym",
        {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        }
      );
      setSalles(response.data);
      setLoading(false);
    } catch (err: any) {
      setError("Erreur lors de la récuperation des salles");
      setLoading(false);
      console.error("Erreur détaillée:", err.response?.data || err.message);
    }
  };

  // Récuperer les details d'une salle par ID
  const fetchSalleDetails = async (id: number) => {
    setDetailLoading(true);
    try {
      const response = await axios.get<Salle>(
        `http://localhost:8080/api/salles/${id}`,
        {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        }
      );
      setSelectedSalle(response.data);
    } catch (err: any) {
      setError("Erreur lors de la récuperation des détails de la salle");
      console.error("Erreur détaillée:", err.response?.data || err.message);
    } finally {
      setDetailLoading(false);
    }
  };

  // Préparer le formulaire de modification
  const prepareEditForm = (salle: Salle) => {
    setEditingSalle(salle);
    setFormData({
      nom: salle.nom || "",
    });
  };

  // Préparer le formulaire d'ajout
  const prepareAddForm = () => {
    setAddingSalle(true);
    setFormData({
      nom: "",
    });
  };

  // Modifier le nom d'une salle
  const handleUpdateSalle = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!editingSalle) return;

    setUpdating(true);
    try {
      const response = await axios.put(
        `http://localhost:8080/api/salles/${editingSalle.id}`,
        {
          nom: formData.nom,
        },
        {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        }
      );
      setSuccess("Salle modifier avec succès !");
      setEditingSalle(null);
      fetchSalles();
      setTimeout(() => setSuccess(""), 3000);
    } catch (err: any) {
      setError(
        `Erreur lors de la modification: ${
          err.response?.data.message || err.message
        }`
      );
    } finally {
      setUpdating(false);
    }
  };

  // Supprimer une salle
  const handleDeleteSalle = async (id: number) => {
    try {
      const response = await axios.delete(
        `http://localhost:8080/api/salles/${id}`,
        {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        }
      );
      setSuccess("Salle supprimé avec succès !");
      setSelectedSalle(null);
      fetchSalles();
      setTimeout(() => setSuccess(""), 3000);
    } catch (err: any) {
      setError(
        `Erreur lors de la suppression ${
          err.response?.data.message || err.message
        }`
      );
    }
  };

  // Ajouter une salle
  const handleAddSalle = async (e: React.FormEvent) => {
    e.preventDefault();

    setAdding(true);
    try {
      const response = await axios.post(
        `http://localhost:8080/api/salles/ajouter`,
        {
          nom: formData.nom,
        },
        {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        }
      );
      setSuccess("Salle ajouté avec succès !");
      setAddingSalle(false);
      fetchSalles();
      setTimeout(() => setSuccess(""), 3000);
    } catch (err: any) {
      setError(
        `Erreur lors de l'ajout: ${err.response?.data.message || err.message}`
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
    setSelectedSalle(null);
  };

  const closeEditForm = () => {
    setEditingSalle(null);
  };

  const closeAddForm = () => {
    setAddingSalle(false);
  };

  // Calcul le nombre de salle à afficher
  const indexOfLastSalle = currentPage * sallesPerPage;
  const indexOfFirstSalle = indexOfLastSalle - sallesPerPage;
  const currentSalle = salles.slice(indexOfFirstSalle, indexOfLastSalle);
  const totalPages = Math.ceil(salles.length / sallesPerPage);

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

  // Gérer le changement du nombre de salle par page
  const handleSallesPerPageChange = (
    e: React.ChangeEvent<HTMLSelectElement>
  ) => {
    setSallesPerPage(Number(e.target.value));
    setCurrentPage(1); // Réinitialiser à la première page
  };

  // Gestion du modal de confirmation
  const openConfirmModal = (id: number, message: string) => {
    setConfirmModal({ isOpen: true, id, message });
  };

  const closeConfirmModal = () => {
    setConfirmModal({ isOpen: false, id: null, message: "" });
  };

  const confirmDelete = () => {
    if (confirmModal.id) {
      handleDeleteSalle(confirmModal.id);
    }
    closeConfirmModal();
  };

  useEffect(() => {
    fetchSalles();
  }, []);

  if (loading) {
    return (
      <div className="min-h-screen bg-black flex items-center justify-center">
        <div className="text-white text-xl">Chargement de la salle...</div>
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
      <header className="fixed top-0 w-full  bg-black text-white flex justify-between items-center px-6 py-4 shadow-md z-50">
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
          <button
            className="text-white hover:text-orange-500 text-xl transition cursor-pointer bg-transparent border-none"
            onClick={() => window.location.reload()} // Recharge la page
          >
            Planning
          </button>
          <div className="relative group">
            <button className="text-white hover:text-orange-500 text-xl transition cursor-pointer bg-transparent border-none">
              Administration
            </button>
            <div className="absolute left-0 mt-1 hidden group-hover:block w-64 bg-black border border-orange-600 rounded shadow-lg z-10 text-xs">
              <button
                onClick={() => navigate("/membres")}
                className="block w-full text-left px-4 py-2 text-white hover:text-orange-400 cursor-pointer"
              >
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
              Gestion des salles
            </h1>
            <p className="text-gray-400">
              {salles.length} salle{salles.length !== 1 ? "s" : ""} trouvé
              {salles.length !== 1 ? "s" : ""} dans votre salle de sport
            </p>
          </div>
          <button
            onClick={prepareAddForm}
            className="bg-orange-500 text-white px-6 py-2 rounded-lg hover:bg-white hover:text-orange-500 transition-colors font-bold"
          >
            + Ajouter une salle
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
        {salles.length > 0 && (
          <div className="mb-4 flex flex-col md:flex-row justify-between items-center space-y-2 md:space-y-0">
            <div className="flex items-center space-x-2">
              <span className="text-white">Salles par page:</span>
              <select
                value={sallesPerPage}
                onChange={handleSallesPerPageChange}
                className="p-1 border rounded bg-white"
              >
                <option value="5">5</option>
                <option value="10">10</option>
                <option value="20">20</option>
                <option value="50">50</option>
              </select>
            </div>

            <div className="text-white">
              Affichage de {indexOfFirstSalle + 1} à{" "}
              {Math.min(indexOfLastSalle, salles.length)} sur {salles.length}{" "}
              salles
            </div>
          </div>
        )}

        {/* Tableau des salles */}
        <div className="bg-white rounded-lg shadow-lg overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-orange-500 text-white">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider">
                    Nom de la salle
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider">
                    Actions
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {currentSalle.map((salle) => (
                  <tr
                    key={salle.id}
                    className="hover:bg-gray-300 cursor-pointer transition-colors odd:bg-gray-100 even:bg-gray-200"
                    onClick={() => fetchSalleDetails(salle.id)}
                  >
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm font-medium text-gray-900">
                        {salle.nom}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <button
                        onClick={(e) => {
                          e.stopPropagation();
                          openConfirmModal(
                            salle.id,
                            `Êtes-vous sûr de vouloir supprimer la salle "${salle.nom}" ? Cette action est irréversible.`
                          );
                        }}
                        className="bg-red-500 text-white px-2 py-1 rounded hover:bg-red-600 transition-colors"
                      >
                        Supprimer
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        {/* Pagination en bas */}
        {salles.length > 0 && (
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

        {salles.length === 0 && (
          <div className="text-center py-8 text-gray-500 bg-white rounded-lg mt-4">
            Aucune salle trouvée dans la base de données.
          </div>
        )}

        {/* Bouton d'actualisation */}
        <div className="mt-6 flex justify-center">
          <button
            onClick={fetchSalles}
            className="bg-orange-500 text-white px-6 py-2 rounded-lg hover:bg-white hover:text-orange-500 transition-colors font-bold"
          >
            Actualiser la liste
          </button>
        </div>

        {/* Modal des détails de la salle */}
        {selectedSalle && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div className="bg-white rounded-lg p-6 w-full max-w-2xl max-h-90vh overflow-y-auto">
              <div className="flex justify-between items-center mb-4">
                <h2 className="text-2xl font-bold">Détails de la salle</h2>
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
                      <div className="space-y-2">
                        <p>
                          <strong>Nom :</strong> {selectedSalle.nom}
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
                        prepareEditForm(selectedSalle);
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

        {/* Modal de modification de la salle */}
        {editingSalle && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div className="bg-white rounded-lg p-6 w-full max-w-2xl max-h-90vh overflow-y-auto">
              <div className="flex justify-between items-center mb-4">
                <h2 className="text-2xl font-bold">Modifier la salle</h2>
                <button
                  onClick={closeEditForm}
                  className="text-gray-500 hover:text-gray-700 text-2xl"
                >
                  ×
                </button>
              </div>

              <form onSubmit={handleUpdateSalle} className="space-y-4">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-gray-700">Nom</label>
                    <input
                      type="text"
                      name="nom"
                      value={formData.nom}
                      onChange={handleChange}
                      className="w-full p-2 border rounded"
                      required
                    />
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

        {/* Modal d'ajout de salle */}
        {addingSalle && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div className="bg-white rounded-lg p-6 w-full max-w-2xl max-h-90vh overflow-y-auto">
              <div className="flex justify-between items-center mb-4">
                <h2 className="text-2xl font-bold">
                  Ajouter une Nouvelle salle
                </h2>
                <button
                  onClick={closeAddForm}
                  className="text-gray-500 hover:text-gray-700 text-2xl"
                >
                  ×
                </button>
              </div>

              <form onSubmit={handleAddSalle} className="space-y-4">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-gray-700">Nom *</label>
                    <input
                      type="text"
                      name="nom"
                      value={formData.nom}
                      onChange={handleChange}
                      className="w-full p-2 border rounded"
                      required
                    />
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
                    className="bg-orange-500 text-white px-6 py-2 rounded-lg hover:bg-orange-400 transition-colors font-bold"
                  >
                    {adding ? "Ajout en cours..." : "Ajouter"}
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}

        {/* Modal de confirmation de suppression */}
        {confirmModal.isOpen && (
          <ConfirmModal
            isOpen={confirmModal.isOpen}
            onClose={closeConfirmModal}
            onConfirm={confirmDelete}
            message={confirmModal.message}
          />
        )}
      </main>
    </div>
  );
}

export default GestionSalle;
