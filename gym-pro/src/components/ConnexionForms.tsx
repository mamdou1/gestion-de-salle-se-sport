import { useState } from "react";
import axios from "axios";

interface ConnexionFormProps {
  setIsLoggedIn: (value: boolean) => void;
  setShowRegister: (value: boolean) => void;
}

interface FormData {
  telephone: string;
  password: string;
}

function ConnexionForm({ setIsLoggedIn, setShowRegister }: ConnexionFormProps) {
  const [formData, setFormData] = useState<FormData>({
    telephone: "",
    password: "",
  });
  const [error, setError] = useState<string>("");
  const [loading, setLoading] = useState<boolean>(false);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError("");

    try {
      const response = await axios.post(
        "http://localhost:8080/api/auth/connexion",
        formData
      );

      // Stockage du token
      localStorage.setItem("token", response.data.token);

      // Stockage sécurisé du gymId
      const user = response.data.user;
      if (user && user.gym && user.gym.id) {
        localStorage.setItem("gymId", String(user.gym.id));
      } else {
        console.warn("gymId non trouvé dans la réponse");
        localStorage.setItem("gymId", "null");
      }

      setIsLoggedIn(true);
    } catch (err: any) {
      setError(
        `Échec de connexion : ${err.response?.data?.message || err.message}`
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex flex-col md:flex-row min-h-screen">
      {/* Partie gauche */}
      <div className="md:w-1/2 bg-orange-500 flex flex-col justify-center items-center p-8 text-white">
        <h1 className="text-4xl font-bold mb-4">GYM-PRO</h1>
        <p className="text-xl text-center">
          Pour une gestion plus rigoureuse
          <br /> de votre salle
        </p>
      </div>

      {/* Partie droite */}
      <div className="md:w-1/2 flex items-center justify-center bg-gray-50">
        <div className="bg-white p-8 rounded-lg shadow-lg w-full max-w-md">
          <h2 className="text-2xl font-bold mb-6 text-center text-gray-800">
            Connexion
          </h2>
          <p className="text-center mb-6 text-sm text-gray-600">
            Identifiez-vous pour accéder à votre espace
          </p>

          {error && (
            <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit}>
            <div className="mb-4">
              <label className="block text-gray-700 mb-1">
                Numéro de téléphone
              </label>
              <input
                type="text"
                name="telephone"
                value={formData.telephone}
                onChange={handleChange}
                className="w-full p-2 border rounded focus:outline-none focus:ring-2 focus:ring-orange-500"
                required
                disabled={loading}
              />
            </div>
            <div className="mb-4">
              <label className="block text-gray-700 mb-1">Mot de passe</label>
              <input
                type="password"
                name="password"
                value={formData.password}
                onChange={handleChange}
                className="w-full p-2 border rounded focus:outline-none focus:ring-2 focus:ring-orange-500"
                required
                disabled={loading}
              />
            </div>

            <div className="flex justify-between mb-4 text-sm text-orange-500">
              <a href="#" className="hover:underline">
                Mot de passe oublié ?
              </a>
              <span
                className="cursor-pointer hover:underline"
                onClick={() => setShowRegister(true)}
              >
                Créer un compte
              </span>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full bg-orange-500 text-white p-2 rounded hover:bg-orange-600 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {loading ? "Connexion..." : "Se connecter"}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}

export default ConnexionForm;
