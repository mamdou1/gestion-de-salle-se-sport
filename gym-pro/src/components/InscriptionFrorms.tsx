import { useState } from "react";
import axios from "axios";

interface InscriptionFormProps {
  setShowRegister: (value: boolean) => void;
  setIsLoggedIn: (value: boolean) => void;
}

interface FormData {
  nomGym: string;
  adresseGym: string;
  telephoneGym: string;
  emailGym: string;
  nomAdmin: string;
  prenomAdmin: string;
  adresseAdmin: string;
  emailAdmin: string;
  genre: "HOMME" | "FEMME";
  date_de_naissance: string;
  telephoneAdmin: string;
  passwordAdmin: string;
  confirmPassword: string;
}

function InscriptionForm({
  setShowRegister,
  setIsLoggedIn,
}: InscriptionFormProps) {
  const [step, setStep] = useState<number>(1);
  const [formData, setFormData] = useState<FormData>({
    nomGym: "",
    adresseGym: "",
    telephoneGym: "",
    emailGym: "",
    nomAdmin: "",
    prenomAdmin: "",
    adresseAdmin: "",
    emailAdmin: "",
    genre: "HOMME",
    date_de_naissance: "",
    telephoneAdmin: "",
    passwordAdmin: "",
    confirmPassword: "",
  });
  const [error, setError] = useState<string>("");

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
  ) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  const handleNext = () => setStep(step + 1);
  const handlePrevious = () => setStep(step - 1);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (formData.passwordAdmin !== formData.confirmPassword) {
      setError("Les mots de passe ne correspondent pas");
      return;
    }
    try {
      const response = await axios.post(
        "http://localhost:8080/api/auth/inscription",
        {
          ...formData,
          date_de_naissance: formData.date_de_naissance || null,
        }
      );
      localStorage.setItem("token", response.data.token);
      setIsLoggedIn(true);
      setShowRegister(false);
    } catch (err: any) {
      setError(
        `Échec de l'inscription : ${err.response?.data?.message || err.message}`
      );
    }
  };

  return (
    <div className="bg-white p-6 rounded-lg shadow-lg w-full max-w-md">
      <h2 className="text-2xl font-bold mb-6 text-center">
        Inscription de votre salle de sport
      </h2>
      {error && <p className="text-red-500 mb-4">{error}</p>}
      <form onSubmit={step === 3 ? handleSubmit : (e) => e.preventDefault()}>
        {step === 1 && (
          <div>
            <h3 className="text-xl font-semibold mb-4">
              Informations de la salle
            </h3>
            <div className="mb-4">
              <label className="block text-gray-700">Nom de la salle</label>
              <input
                type="text"
                name="nomGym"
                value={formData.nomGym}
                onChange={handleChange}
                className="w-full p-2 border rounded"
                required
              />
            </div>
            <div className="mb-4">
              <label className="block text-gray-700">Adresse de la salle</label>
              <input
                type="text"
                name="adresseGym"
                value={formData.adresseGym}
                onChange={handleChange}
                className="w-full p-2 border rounded"
                required
              />
            </div>
            <div className="mb-4">
              <label className="block text-gray-700">
                Téléphone de la salle
              </label>
              <input
                type="text"
                name="telephoneGym"
                value={formData.telephoneGym}
                onChange={handleChange}
                className="w-full p-2 border rounded"
                required
              />
            </div>
            <div className="mb-4">
              <label className="block text-gray-700">Email de la salle</label>
              <input
                type="email"
                name="emailGym"
                value={formData.emailGym}
                onChange={handleChange}
                className="w-full p-2 border rounded"
                required
              />
            </div>
            <button
              type="button"
              onClick={handleNext}
              className="w-full bg-orange-500 text-white p-2 rounded hover:bg-orange-600 transition-colors"
            >
              Suivant
            </button>
          </div>
        )}
        {step === 2 && (
          <div>
            <h3 className="text-xl font-semibold mb-4">
              Informations de l'administrateur
            </h3>
            <div className="mb-4">
              <label className="block text-gray-700">Nom</label>
              <input
                type="text"
                name="nomAdmin"
                value={formData.nomAdmin}
                onChange={handleChange}
                className="w-full p-2 border rounded"
                required
              />
            </div>
            <div className="mb-4">
              <label className="block text-gray-700">Prénom</label>
              <input
                type="text"
                name="prenomAdmin"
                value={formData.prenomAdmin}
                onChange={handleChange}
                className="w-full p-2 border rounded"
                required
              />
            </div>
            <div className="mb-4">
              <label className="block text-gray-700">Adresse</label>
              <input
                type="text"
                name="adresseAdmin"
                value={formData.adresseAdmin}
                onChange={handleChange}
                className="w-full p-2 border rounded"
                required
              />
            </div>
            <div className="mb-4">
              <label className="block text-gray-700">Email</label>
              <input
                type="email"
                name="emailAdmin"
                value={formData.emailAdmin}
                onChange={handleChange}
                className="w-full p-2 border rounded"
                required
              />
            </div>
            <div className="mb-4">
              <label className="block text-gray-700">Genre</label>
              <select
                name="genre"
                value={formData.genre}
                onChange={handleChange}
                className="w-full p-2 border rounded"
              >
                <option value="HOMME">Homme</option>
                <option value="FEMME">Femme</option>
              </select>
            </div>
            <div className="mb-4">
              <label className="block text-gray-700">Date de naissance</label>
              <input
                type="date"
                name="date_de_naissance"
                value={formData.date_de_naissance}
                onChange={handleChange}
                className="w-full p-2 border rounded"
              />
            </div>
            <div className="flex justify-between">
              <button
                type="button"
                onClick={handlePrevious}
                className="bg-gray-500 text-white p-2 rounded hover:bg-gray-600"
              >
                Précédent
              </button>
              <button
                type="button"
                onClick={handleNext}
                className="w-full bg-orange-500 text-white p-2 rounded hover:bg-orange-600 transition-colors"
              >
                Suivant
              </button>
            </div>
          </div>
        )}
        {step === 3 && (
          <div>
            <h3 className="text-xl font-semibold mb-4">Détails du compte</h3>
            <div className="mb-4">
              <label className="block text-gray-700">Numéro de téléphone</label>
              <input
                type="text"
                name="telephoneAdmin"
                value={formData.telephoneAdmin}
                onChange={handleChange}
                className="w-full p-2 border rounded"
                required
              />
            </div>
            <div className="mb-4">
              <label className="block text-gray-700">Mot de passe</label>
              <input
                type="password"
                name="passwordAdmin"
                value={formData.passwordAdmin}
                onChange={handleChange}
                className="w-full p-2 border rounded"
                required
              />
            </div>
            <div className="mb-4">
              <label className="block text-gray-700">
                Confirmer le mot de passe
              </label>
              <input
                type="password"
                name="confirmPassword"
                value={formData.confirmPassword}
                onChange={handleChange}
                className="w-full p-2 border rounded"
                required
              />
            </div>
            <div className="flex justify-between">
              <button
                type="button"
                onClick={handlePrevious}
                className="bg-gray-500 text-white p-2 rounded hover:bg-gray-600"
              >
                Précédent
              </button>
              <button
                type="submit"
                className="w-full bg-orange-500 text-white p-2 rounded hover:bg-orange-600 transition-colors"
              >
                Soumettre
              </button>
            </div>
          </div>
        )}
        <p className="mt-4 text-center">
          Déjà un compte ?{" "}
          <span
            className="text-orange-500 cursor-pointer"
            onClick={() => setShowRegister(false)}
          >
            Se connecter
          </span>
        </p>
      </form>
    </div>
  );
}

export default InscriptionForm;
