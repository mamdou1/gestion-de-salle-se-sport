import React, { useState, useEffect } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";

interface Produit {
  id: number;
  nom: string;
  description: string;
  prixUnitaire: number;
  quantiteEnStock: number;
  imageUrl: string;
  categorie: "EQUIPEMENT" | "ALIMENTATION";
  gym?: number;
}

interface FormData {
  nom: string;
  description: string;
  prixUnitaire: number;
  quantiteEnStock: number;
  imageUrl: string;
  categorie: "EQUIPEMENT" | "ALIMENTATION";
}

interface TableauDeBordProps {
  setIsLoggedIn: (value: boolean) => void;
}

function GestionProduit({ setIsLoggedIn }: TableauDeBordProps) {
  const [produits, setProduits] = useState<Produit[]>([]);
  const [selectedProduit, setSelectedProduit] = useState<Produit | null>(null);
  const [editingProduit, setEditingProduit] = useState<Produit | null>(null);
  const [addingProduit, setAddingProduit] = useState<boolean>(false);
  const [formData, setFormData] = useState<FormData>({
    nom: "",
    description: "",
    prixUnitaire: 0,
    quantiteEnStock: 0,
    imageUrl: "",
    categorie: "EQUIPEMENT",
  });
  const [loading, setLoading] = useState<boolean>(true);
  const [detailLoading, setDetailLoading] = useState<boolean>(false);
  const [updating, setUpdating] = useState<boolean>(false);
  const [adding, setAdding] = useState<boolean>(false);
  const [error, setError] = useState<string>("");
  const [success, setSuccess] = useState<string>("");
  const [currentPage, setCurrentPage] = useState<number>(1);
  const productsPerPage = 10;
  const navigate = useNavigate();

  const token = localStorage.getItem("token");

  const handleLogout = () => {
    localStorage.removeItem("token");
    setIsLoggedIn(false);
  };

  const fetchProduit = async () => {
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
      setLoading(false);
    } catch (err: any) {
      setError("Erreur lors de la récupération des produits.");
      setLoading(false);
      console.error("Erreur détaillée:", err.response?.data || err.message);
    }
  };

  const fetchProduitDetails = async (id: number) => {
    setDetailLoading(true);
    try {
      const response = await axios.get<Produit>(
        `http://localhost:8080/api/produits/${id}`,
        {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        }
      );
      setSelectedProduit(response.data);
    } catch (err: any) {
      setError("Erreur lors de la récupération des détails du produit.");
      console.error("Erreur détaillée:", err.response?.data || err.message);
    } finally {
      setDetailLoading(false);
    }
  };

  // fonction pour supprimer un produit
  const handleDeleteProduit = async (id: number) => {
    if (!window.confirm("Êtes-vous sûr de vouloir supprimer ce produit ?")) {
      return;
    }
    try {
      await axios.delete(`http://localhost:8080/api/produits/supprimer/${id}`, {
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
      });
      setSuccess("Produit supprimé avec succès !");
      setSelectedProduit(null); // Ferme la modal des détails si ouverte
      fetchProduit(); // Rafraîchit la liste
      setTimeout(() => setSuccess(""), 3000);
    } catch (err: any) {
      setError(
        `Erreur lors de la suppression : ${
          err.response?.data?.message || err.message
        }`
      );
    }
  };

  const prepareEditForm = (produit: Produit) => {
    setEditingProduit(produit);
    setFormData({
      nom: produit.nom || "",
      description: produit.description || "",
      prixUnitaire: produit.prixUnitaire || 0,
      quantiteEnStock: produit.quantiteEnStock || 0,
      imageUrl: produit.imageUrl || "",
      categorie: produit.categorie || "EQUIPEMENT",
    });
  };

  const prepareAddForm = () => {
    setAddingProduit(true);
    setFormData({
      nom: "",
      description: "",
      prixUnitaire: 0,
      quantiteEnStock: 0,
      imageUrl: "",
      categorie: "EQUIPEMENT",
    });
  };

  const handleUpdateProduit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!editingProduit) return;
    if (formData.prixUnitaire < 0 || formData.quantiteEnStock < 0) {
      setError("Le prix et la quantité doivent être positifs.");
      return;
    }

    setUpdating(true);
    try {
      await axios.put(
        `http://localhost:8080/api/produits/modifier/${editingProduit.id}`,
        {
          nom: formData.nom,
          description: formData.description,
          prixUnitaire: formData.prixUnitaire,
          quantiteEnStock: formData.quantiteEnStock,
          imageUrl: formData.imageUrl,
          categorie: formData.categorie,
        },
        {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        }
      );

      setSuccess("Produit modifié avec succès !");
      setEditingProduit(null);
      fetchProduit();
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

  const handleAddProduit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (formData.prixUnitaire < 0 || formData.quantiteEnStock < 0) {
      setError("Le prix et la quantité doivent être positifs.");
      return;
    }

    setAdding(true);
    try {
      await axios.post(
        "http://localhost:8080/api/produits/ajouter",
        {
          nom: formData.nom,
          description: formData.description,
          prixUnitaire: formData.prixUnitaire,
          quantiteEnStock: formData.quantiteEnStock,
          imageUrl: formData.imageUrl,
          categorie: formData.categorie,
        },
        {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        }
      );

      setSuccess("Produit ajouté avec succès !");
      setAddingProduit(false);
      fetchProduit();
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
    setFormData((prev) => ({
      ...prev,
      [name]:
        name === "prixUnitaire" || name === "quantiteEnStock"
          ? parseFloat(value)
          : value,
    }));
  };

  const closeDetails = () => {
    setSelectedProduit(null);
  };

  const closeEditForm = () => {
    setEditingProduit(null);
  };

  const closeAddForm = () => {
    setAddingProduit(false);
  };

  const indexOfLastProduct = currentPage * productsPerPage;
  const indexOfFirstProduct = indexOfLastProduct - productsPerPage;
  const currentProducts = produits.slice(
    indexOfFirstProduct,
    indexOfLastProduct
  );
  const totalPages = Math.ceil(produits.length / productsPerPage);

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
    fetchProduit();
  }, []);

  if (loading) {
    return (
      <div className="min-h-screen bg-black flex items-center justify-center">
        <div className="text-white text-xl">Chargement des produits...</div>
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
      <header className="bg-black text-white flex justify-between items-center px-6 py-4 shadow-md">
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
                Gestion des produits
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
              Gestion des Produits
            </h1>
            <p className="text-gray-400">
              {produits.length} produit{produits.length !== 1 ? "s" : ""} trouvé
              {produits.length !== 1 ? "s" : ""} dans votre salle de sport
            </p>
          </div>
          <button
            onClick={prepareAddForm}
            className="bg-orange-500 text-white px-6 py-2 rounded-lg hover:bg-white hover:text-orange-500 transition-colors font-bold"
          >
            + Ajouter un produit
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
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-orange-500 text-white">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider">
                    Photo
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider">
                    Nom
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider">
                    Description
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider">
                    Prix unitaire
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider">
                    Catégorie
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider">
                    Quantité en stock
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider">
                    Actions
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {currentProducts.map((produit) => (
                  <tr
                    key={produit.id}
                    className="hover:bg-gray-300 cursor-pointer transition-colors odd:bg-gray-100 even:bg-gray-200"
                    onClick={() => fetchProduitDetails(produit.id)}
                  >
                    <td className="px-6 py-4 whitespace-nowrap">
                      <img
                        src={
                          produit.imageUrl || "https://via.placeholder.com/40"
                        }
                        alt={produit.nom}
                        className="w-10 h-10 rounded-full object-cover"
                      />
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm font-medium text-gray-900">
                        {produit.nom}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm text-gray-900">
                        {produit.description}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm text-gray-900">
                        {produit.prixUnitaire} FCFA
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span
                        className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${
                          produit.categorie === "EQUIPEMENT"
                            ? "bg-blue-100 text-blue-800"
                            : "bg-pink-100 text-pink-800"
                        }`}
                      >
                        {produit.categorie}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm text-gray-900">
                        {produit.quantiteEnStock}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <button
                        onClick={(e) => {
                          e.stopPropagation(); // Empêche le clic sur la ligne d'ouvrir les détails
                          handleDeleteProduit(produit.id);
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

        {produits.length > 0 && (
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

        {produits.length === 0 && (
          <div className="text-center py-8 text-gray-500 bg-white rounded-lg mt-4">
            Aucun produit trouvé dans la base de données.
          </div>
        )}

        <div className="mt-6 flex justify-center">
          <button
            onClick={fetchProduit}
            className="bg-orange-500 text-white px-6 py-2 rounded-lg hover:bg-orange-600 transition-colors"
          >
            Actualiser la liste
          </button>
        </div>

        {selectedProduit && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div className="bg-white rounded-lg p-6 w-full max-w-2xl max-h-90vh overflow-y-auto">
              <div className="flex justify-between items-center mb-4">
                <h2 className="text-2xl font-bold">Détails du Produit</h2>
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
                        Informations du produit
                      </h3>
                      <div className="space-y-2">
                        <div className="mb-4">
                          <img
                            src={
                              selectedProduit.imageUrl ||
                              "https://via.placeholder.com/100"
                            }
                            alt={selectedProduit.nom}
                            className="w-24 h-24 rounded-full object-cover mx-auto"
                          />
                        </div>
                        <p>
                          <strong>Nom:</strong> {selectedProduit.nom}
                        </p>
                        <p>
                          <strong>Description:</strong>{" "}
                          {selectedProduit.description}
                        </p>
                        <p>
                          <strong>Prix unitaire:</strong>{" "}
                          {selectedProduit.prixUnitaire} FCFA
                        </p>
                        <p>
                          <strong>Catégorie:</strong>{" "}
                          {selectedProduit.categorie}
                        </p>
                        <p>
                          <strong>Quantité en stock:</strong>{" "}
                          {selectedProduit.quantiteEnStock}
                        </p>
                      </div>
                    </div>
                  </div>

                  <div className="flex justify-end space-x-3">
                    <button
                      onClick={() => {
                        prepareEditForm(selectedProduit);
                        closeDetails();
                      }}
                      className="bg-orange-500 text-white px-6 py-2 rounded-lg hover:bg-orange-400 transition-colors font-bold"
                    >
                      Modifier
                    </button>
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

        {editingProduit && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div className="bg-white rounded-lg p-6 w-full max-w-2xl max-h-90vh overflow-y-auto">
              <div className="flex justify-between items-center mb-4">
                <h2 className="text-2xl font-bold">Modifier le Produit</h2>
                <button
                  onClick={closeEditForm}
                  className="text-gray-500 hover:text-gray-700 text-2xl"
                >
                  ×
                </button>
              </div>

              <form onSubmit={handleUpdateProduit} className="space-y-4">
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
                  <div>
                    <label className="block text-gray-700">Description *</label>
                    <input
                      type="text"
                      name="description"
                      value={formData.description}
                      onChange={handleChange}
                      className="w-full p-2 border rounded"
                      required
                    />
                  </div>
                  <div>
                    <label className="block text-gray-700">
                      Prix unitaire *
                    </label>
                    <input
                      type="number"
                      name="prixUnitaire"
                      value={formData.prixUnitaire}
                      onChange={handleChange}
                      className="w-full p-2 border rounded"
                      required
                    />
                  </div>
                  <div>
                    <label className="block text-gray-700">
                      Quantité en stock *
                    </label>
                    <input
                      type="number"
                      name="quantiteEnStock"
                      value={formData.quantiteEnStock}
                      onChange={handleChange}
                      className="w-full p-2 border rounded"
                      required
                    />
                  </div>
                  <div>
                    <label className="block text-gray-700">Catégorie *</label>
                    <select
                      name="categorie"
                      value={formData.categorie}
                      onChange={handleChange}
                      className="w-full p-2 border rounded"
                      required
                    >
                      <option value="EQUIPEMENT">Équipement</option>
                      <option value="ALIMENTATION">Alimentation</option>
                    </select>
                  </div>
                  <div>
                    <label className="block text-gray-700">
                      URL de la photo
                    </label>
                    <input
                      type="text"
                      name="imageUrl"
                      value={formData.imageUrl}
                      onChange={handleChange}
                      className="w-full p-2 border rounded"
                      placeholder="https://example.com/photo.jpg"
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

        {addingProduit && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div className="bg-white rounded-lg p-6 w-full max-w-2xl max-h-90vh overflow-y-auto">
              <div className="flex justify-between items-center mb-4">
                <h2 className="text-2xl font-bold">
                  Ajouter un Nouveau Produit
                </h2>
                <button
                  onClick={closeAddForm}
                  className="text-gray-500 hover:text-gray-700 text-2xl"
                >
                  ×
                </button>
              </div>

              <form onSubmit={handleAddProduit} className="space-y-4">
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
                  <div>
                    <label className="block text-gray-700">Description *</label>
                    <input
                      type="text"
                      name="description"
                      value={formData.description}
                      onChange={handleChange}
                      className="w-full p-2 border rounded"
                      required
                    />
                  </div>
                  <div>
                    <label className="block text-gray-700">
                      Prix unitaire *
                    </label>
                    <input
                      type="number"
                      name="prixUnitaire"
                      value={formData.prixUnitaire}
                      onChange={handleChange}
                      className="w-full p-2 border rounded"
                      required
                    />
                  </div>
                  <div>
                    <label className="block text-gray-700">
                      Quantité en stock *
                    </label>
                    <input
                      type="number"
                      name="quantiteEnStock"
                      value={formData.quantiteEnStock}
                      onChange={handleChange}
                      className="w-full p-2 border rounded"
                      required
                    />
                  </div>
                  <div>
                    <label className="block text-gray-700">Catégorie *</label>
                    <select
                      name="categorie"
                      value={formData.categorie}
                      onChange={handleChange}
                      className="w-full p-2 border rounded"
                      required
                    >
                      <option value="EQUIPEMENT">Équipement</option>
                      <option value="ALIMENTATION">Alimentation</option>
                    </select>
                  </div>
                  <div>
                    <label className="block text-gray-700">
                      URL de la photo
                    </label>
                    <input
                      type="text"
                      name="imageUrl"
                      value={formData.imageUrl}
                      onChange={handleChange}
                      className="w-full p-2 border rounded"
                      placeholder="https://example.com/photo.jpg"
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
      </main>
    </div>
  );
}

export default GestionProduit;
