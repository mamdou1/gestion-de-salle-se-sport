import { useState } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom"; // Ajout de useNavigate

interface InscriptionFormProps {
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

function InscriptionForm({ setIsLoggedIn }: InscriptionFormProps) {
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
  const [success, setSuccess] = useState<string>("");
  const navigate = useNavigate(); // Hook pour la navigation
  const [verificationCode, setVerificationCode] = useState<string>("");
  const [isVerifying, setIsVerifying] = useState<boolean>(false);

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
  ) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  const handleNext = () => setStep(step + 1);
  const handlePrevious = () => setStep(step - 1);

  const handleSubmitStep3 = async (e: React.FormEvent) => {
    e.preventDefault();
    if (formData.passwordAdmin !== formData.confirmPassword) {
      setError("Les mots de passe ne correspondent pas");
      return;
    }
    setStep(4); // Passe directement à l'étape 4 après validation
  };

  const handleSubmitStep4 = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const response = await axios.post(
        "http://localhost:8080/api/auth/inscription",
        {
          ...formData,
          date_de_naissance: formData.date_de_naissance || null,
        }
      );

      // Stocker le token reçu
      if (response.data.token) {
        localStorage.setItem("authToken", response.data.token);
      }
      setSuccess("Un code de validation a été envoyé à votre email.");
      setTimeout(() => {
        setSuccess(""); // Masque le message après 5 secondes
        setStep(5); // Passe à l'étape 5 après succès
      }, 5000);
    } catch (err: any) {
      setError(
        `Échec de l'inscription : ${err.response?.data?.message || err.message}`
      );
    }
  };

  // Ajoutez cette fonction pour récupérer le token
  const getAuthToken = (): string | null => {
    return localStorage.getItem("authToken");
  };

  const handleVerifyCode = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsVerifying(true);

    const token = getAuthToken();
    if (!token) {
      setError("Token d'authentification manquant. Veuillez réessayer.");
      setIsVerifying(false);
      return;
    }

    try {
      const response = await axios.post(
        "http://localhost:8080/api/auth/verifier-compte",
        {
          verificationCode: verificationCode,
        },
        {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        }
      );

      setSuccess("Compte vérifié avec succès ! Vous serez redirigé...");
      setTimeout(() => {
        setIsLoggedIn(true);
        window.location.href = "/connexion";
      }, 2000);
    } catch (err: any) {
      const errorMessage =
        err.response?.data?.error ||
        err.response?.data?.message ||
        err.message ||
        "Une erreur inattendue s'est produite";
      setError(`Échec de la vérification : ${errorMessage}`);
    } finally {
      setIsVerifying(false);
    }
  };

  const handleResendCode = async () => {
    const token = getAuthToken();
    if (!token) {
      setError("Token d'authentification manquant. Veuillez réessayer.");
      return;
    }

    try {
      await axios.post(
        "http://localhost:8080/api/auth/renvoyer-code",
        {},
        {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        }
      );
      setSuccess("Un nouveau code a été envoyé à votre email.");
      setTimeout(() => setSuccess(""), 5000);
    } catch (err: any) {
      const errorMessage =
        err.response?.data?.error ||
        err.response?.data?.message ||
        err.message ||
        "Une erreur inattendue s'est produite";
      setError(`Échec de l'envoi du code : ${errorMessage}`);
    }
  };

  return (
    <div className="min-h-screen bg-gray-200 flex items-center justify-center">
      <div className="bg-white p-6 rounded-lg shadow-lg w-full max-w-md">
        <h2 className="text-orange-500 text-2xl font-bold mb-6 text-center">
          Inscription de votre salle de sport
        </h2>

        {/* Barre de progression */}
        <div className="mb-6">
          <div className="flex justify-between mb-2">
            {Array.from({ length: 5 }, (_, i) => i + 1).map((stepNum) => (
              <div
                key={stepNum}
                className={`flex-1 text-center ${
                  stepNum <= step
                    ? "text-orange-500 font-bold"
                    : "text-gray-400"
                }`}
              >
                {stepNum}
              </div>
            ))}
          </div>
          <div className="w-full bg-gray-200 rounded-full h-2.5">
            <div
              className="bg-orange-500 h-2.5 rounded-full transition-all duration-300"
              style={{ width: `${(step / 5) * 100}%` }}
            ></div>
          </div>
        </div>

        {error && <p className="text-red-500 mb-4">{error}</p>}
        {success && <p className="text-green-500 mb-4">{success}</p>}
        <form
          onSubmit={
            step === 3
              ? handleSubmitStep3
              : step === 4
              ? handleSubmitStep4
              : step === 5
              ? handleVerifyCode
              : (e) => e.preventDefault()
          }
        >
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
                <label className="block text-gray-700">
                  Adresse de la salle
                </label>
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
                <label className="block text-gray-700">
                  Numéro de téléphone
                </label>
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
                  Suivant
                </button>
              </div>
            </div>
          )}
          {step === 4 && (
            <div>
              <h3 className="text-xl font-semibold mb-4">Récapitulatif</h3>
              <div className="mb-4">
                <h4 className="text-orange-500 font-semibold">
                  Informations de la salle :
                </h4>
                <p>
                  <strong>Nom du Gym:</strong> {formData.nomGym}
                </p>
                <p>
                  <strong>Adresse :</strong>
                  {formData.adresseGym}
                </p>
                <p>
                  <strong>Téléphone :</strong> {formData.telephoneGym}
                </p>
                <p>
                  <strong>Email :</strong> {formData.emailGym}
                </p>
              </div>
              <div className="mb-4">
                <h4 className="text-orange-500 font-semibold">
                  Informations de l'administrateur :
                </h4>
                <p>
                  <strong>Nom :</strong>
                  {formData.nomAdmin} {formData.prenomAdmin}
                </p>
                <p>
                  <strong>Adresse :</strong> {formData.adresseAdmin}
                </p>
                <p>
                  <strong>Email : </strong>
                  {formData.emailAdmin}
                </p>
                <p>
                  <strong>Genre :</strong> {formData.genre}
                </p>
                <p>
                  <strong>Date de naissance :</strong>{" "}
                  {formData.date_de_naissance || "Non spécifiée"}
                </p>
              </div>
              <div className="mb-4">
                <h4 className="text-orange-500 font-semibold">
                  Les indentifiant du compte :
                </h4>
                <p>
                  <strong>Téléphone :</strong> {formData.telephoneAdmin}
                </p>
                <p>
                  <strong>Mot de passe :</strong> **** (caché pour sécurité)
                </p>
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
          {step === 5 && (
            <div>
              <h3 className="text-xl font-semibold mb-4">
                Vérification du compte
              </h3>
              <p className="mb-4">
                Un code de validation a été envoyé à {formData.emailAdmin}.
                Veuillez le saisir ci-dessous.
              </p>
              <div className="mb-4">
                <label className="block text-gray-700">
                  Code de validation
                </label>
                <input
                  type="text"
                  value={verificationCode}
                  onChange={(e) => setVerificationCode(e.target.value)}
                  className="w-full p-2 border rounded"
                  required
                />
              </div>
              <div className="flex justify-between">
                <button
                  type="button"
                  onClick={handleResendCode}
                  className="bg-gray-500 text-white p-2 rounded hover:bg-gray-600"
                >
                  Renvoyer le code
                </button>
                <button
                  type="submit"
                  disabled={isVerifying}
                  className="w-full bg-orange-500 text-white p-2 rounded hover:bg-orange-600 transition-colors disabled:opacity-50"
                >
                  {isVerifying ? "Vérification..." : "Vérifier"}
                </button>
              </div>
            </div>
          )}
          <p className="mt-4 text-center">
            Déjà un compte ?{" "}
            <span
              className="text-orange-500 cursor-pointer hover:underline"
              onClick={() => navigate("/connexion")} // Redirige vers /connexion
            >
              Se connecter
            </span>
          </p>
        </form>
      </div>
    </div>
  );
}

export default InscriptionForm;
