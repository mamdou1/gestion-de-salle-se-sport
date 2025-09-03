import { useState, useEffect } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import { Calendar, momentLocalizer, Views, View } from "react-big-calendar";
import moment from "moment";
import "react-big-calendar/lib/css/react-big-calendar.css";
import "moment/locale/fr";
import {
  Coaching,
  CoachingFormData,
  User,
  fetchCoachings as fetchCoachingsAPI,
  fetchCoachingById as fetchCoachingByIdAPI,
  handleAddCoaching as handleAddCoachingAPI,
  handleUpdateCoaching as handleUpdateCoachingAPI,
  handleDeleteCoaching as handleDeleteCoachingAPI,
  fetchCoaches as fetchCoachesAPI,
  fetchClients as fetchClientsAPI,
} from "./Coaching";

// Composant Toast pour les notifications
const Toast = ({
  message,
  type,
  onClose,
}: {
  message: string;
  type: "success" | "error" | "info";
  onClose: () => void;
}) => {
  useEffect(() => {
    const timer = setTimeout(() => {
      onClose();
    }, 3000);

    return () => clearTimeout(timer);
  }, [onClose]);

  const bgColor =
    type === "success"
      ? "bg-green-500"
      : type === "error"
      ? "bg-red-500"
      : "bg-blue-500";

  return (
    <div
      className={`fixed top-4 right-4 ${bgColor} text-white px-6 py-3 rounded-lg shadow-lg z-50 animate-slide-in`}
    >
      <div className="flex items-center">
        <span className="mr-2">{message}</span>
        <button onClick={onClose} className="text-white text-lg font-bold">
          &times;
        </button>
      </div>
    </div>
  );
};

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

// Composant Modal de détail pour l'événement
const EventDetailModal = ({
  isOpen,
  onClose,
  event,
  onEdit,
  onDelete,
}: {
  isOpen: boolean;
  onClose: () => void;
  event: Evenement | null;
  onEdit: () => void;
  onDelete: () => void;
}) => {
  if (!isOpen || !event) return null;

  const getStatusColor = (statut: string) => {
    switch (statut) {
      case "EN_ATTENTE":
        return "bg-yellow-100 text-yellow-800";
      case "EN_COURS":
        return "bg-green-100 text-green-800";
      case "TERMINER":
        return "bg-gray-100 text-gray-800";
      default:
        return "bg-blue-100 text-blue-800";
    }
  };

  const getStatusText = (statut: string) => {
    switch (statut) {
      case "EN_ATTENTE":
        return "En attente";
      case "EN_COURS":
        return "En cours";
      case "TERMINER":
        return "Terminé";
      default:
        return statut;
    }
  };

  // Les details d'un Event
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
      <div className="bg-white rounded-lg p-6 w-full max-w-2xl">
        <div className="flex justify-between items-center mb-6">
          <h2 className="text-2xl font-bold text-gray-800">
            Détails de l'événement
          </h2>
          <button
            onClick={onClose}
            className="text-gray-500 hover:text-gray-700 text-2xl"
          >
            &times;
          </button>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div>
            <h3 className="text-lg font-semibold text-gray-700 mb-4">
              Informations générales
            </h3>

            <div className="space-y-3">
              <div>
                <label className="block text-sm font-medium text-gray-600">
                  Nom
                </label>
                <p className="text-lg font-semibold text-gray-800">
                  {event.nom}
                </p>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-600">
                  Description
                </label>
                <p className="text-gray-700 whitespace-pre-wrap">
                  {event.description || "Aucune description"}
                </p>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-600">
                  Statut
                </label>
                <span
                  className={`inline-block px-3 py-1 rounded-full text-sm font-medium ${getStatusColor(
                    event.statutEvent
                  )}`}
                >
                  {getStatusText(event.statutEvent)}
                </span>
              </div>
            </div>
          </div>

          <div>
            <h3 className="text-lg font-semibold text-gray-700 mb-4">
              Dates et créateur
            </h3>

            <div className="space-y-3">
              <div>
                <label className="block text-sm font-medium text-gray-600">
                  Date de début
                </label>
                <p className="text-gray-700">
                  {moment(event.dateDebut).format(
                    "dddd DD MMMM YYYY [à] HH:mm"
                  )}
                </p>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-600">
                  Date de fin
                </label>
                <p className="text-gray-700">
                  {event.dateFin
                    ? moment(event.dateFin).format(
                        "dddd DD MMMM YYYY [à] HH:mm"
                      )
                    : "Aucune date de fin"}
                </p>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-600">
                  Durée
                </label>
                <p className="text-gray-700">
                  {event.dateFin
                    ? moment
                        .duration(
                          moment(event.dateFin).diff(moment(event.dateDebut))
                        )
                        .humanize()
                    : "Événement ponctuel"}
                </p>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-600">
                  Créé par
                </label>
                <p className="text-gray-700">{event.createdByName}</p>
              </div>
            </div>
          </div>
        </div>

        <div className="flex justify-end space-x-3 mt-8 pt-6 border-t border-gray-200">
          <button
            onClick={onClose}
            className="bg-black text-orange-500 px-4 py-2 rounded hover:bg-gray-600 transition-colors"
          >
            Fermer
          </button>
          <button
            onClick={onEdit}
            className="bg-orange-500 text-white px-6 py-2 rounded-lg hover:bg-orange-400 transition-colors font-bold"
          >
            Modifier
          </button>
        </div>
      </div>
    </div>
  );
};

// Composant Modal de détail pour le coaching
const CoachingDetailModal = ({
  isOpen,
  onClose,
  coaching,
  onEdit,
  onDelete,
}: {
  isOpen: boolean;
  onClose: () => void;
  coaching: Coaching | null;
  onEdit: () => void;
  onDelete: () => void;
}) => {
  if (!isOpen || !coaching) return null;

  // Les details d'un coaching

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
      <div className="bg-white rounded-lg p-6 w-full max-w-2xl">
        <div className="flex justify-between items-center mb-6">
          <h2 className="text-2xl font-bold text-gray-800">
            Détails de la séance de coaching
          </h2>
          <button
            onClick={onClose}
            className="text-gray-500 hover:text-gray-700 text-2xl"
          >
            &times;
          </button>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div>
            <h3 className="text-lg font-semibold text-gray-700 mb-4">
              Informations générales
            </h3>

            <div className="space-y-3">
              <div>
                <label className="block text-sm font-medium text-gray-600">
                  Nom du cours
                </label>
                <p className="text-lg font-semibold text-gray-800">
                  {coaching.nomCours}
                </p>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-600">
                  Description
                </label>
                <p className="text-gray-700 whitespace-pre-wrap">
                  {coaching.description || "Aucune description"}
                </p>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-600">
                  Prix
                </label>
                <p className="text-gray-700">{coaching.prix} FCFA</p>
              </div>
            </div>
          </div>

          <div>
            <h3 className="text-lg font-semibold text-gray-700 mb-4">
              Participants et dates
            </h3>

            <div className="space-y-3">
              <div>
                <label className="block text-sm font-medium text-gray-600">
                  Coach
                </label>
                <p className="text-gray-700">{coaching.nomCoach}</p>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-600">
                  Client
                </label>
                <p className="text-gray-700">{coaching.nomClient}</p>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-600">
                  Date de début
                </label>
                <p className="text-gray-700">
                  {moment(coaching.dateDebut).format(
                    "dddd DD MMMM YYYY [à] HH:mm"
                  )}
                </p>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-600">
                  Date de fin
                </label>
                <p className="text-gray-700">
                  {coaching.dateFin
                    ? moment(coaching.dateFin).format(
                        "dddd DD MMMM YYYY [à] HH:mm"
                      )
                    : "Aucune date de fin"}
                </p>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-600">
                  Durée
                </label>
                <p className="text-gray-700">
                  {coaching.dateFin
                    ? moment
                        .duration(
                          moment(coaching.dateFin).diff(
                            moment(coaching.dateDebut)
                          )
                        )
                        .humanize()
                    : "Séance ponctuelle"}
                </p>
              </div>
            </div>
          </div>
        </div>

        <div className="flex justify-end space-x-3 mt-8 pt-6 border-t border-gray-200">
          <button
            onClick={onClose}
            className="bg-black text-orange-500 px-4 py-2 rounded hover:bg-gray-600 transition-colors"
          >
            Fermer
          </button>
          <button
            onClick={onEdit}
            className="bg-orange-500 text-white px-6 py-2 rounded-lg hover:bg-orange-400 transition-colors font-bold"
          >
            Modifier
          </button>
        </div>
      </div>
    </div>
  );
};

const localizer = momentLocalizer(moment);

const frenchMessages = {
  date: "Date",
  time: "Heure",
  event: "Événement",
  allDay: "Toute la journée",
  week: "Semaine",
  work_week: "Semaine de travail",
  day: "Jour",
  month: "Mois",
  previous: "◀",
  next: "▶",
  yesterday: "Hier",
  tomorrow: "Demain",
  today: "Aujourd'hui",
  agenda: "Agenda",
  noEventsInRange: "Aucun événement dans cette période",
  showMore: (total: number) => `+ ${total} événement(s) supplémentaire(s)`,
  am: "AM",
  pm: "PM",
};

interface Evenement {
  id: number;
  gymId: number;
  nom: string;
  description: string;
  statutEvent: "EN_ATTENTE" | "EN_COURS" | "TERMINER";
  dateDebut: string;
  dateFin: string;
  createdByName: string;
}

interface EvenementFormData {
  id?: number;
  nom: string;
  description: string;
  dateDebut: string;
  dateFin: string;
}

interface TableauDeBordProps {
  setIsLoggedIn: (value: boolean) => void;
}

function Evenement({ setIsLoggedIn }: TableauDeBordProps) {
  const [evenements, setEvenements] = useState<Evenement[]>([]);
  const [coachings, setCoachings] = useState<Coaching[]>([]);
  const [clients, setClients] = useState<User[]>([]);
  const [coaches, setCoaches] = useState<User[]>([]);
  const [showAddForm, setShowAddForm] = useState<boolean>(false);
  const [showEditForm, setShowEditForm] = useState<boolean>(false);
  const [showAddCoachingForm, setShowAddCoachingForm] =
    useState<boolean>(false);
  const [showEditCoachingForm, setShowEditCoachingForm] =
    useState<boolean>(false);
  const [showDetailModal, setShowDetailModal] = useState<boolean>(false);
  const [showCoachingDetailModal, setShowCoachingDetailModal] =
    useState<boolean>(false);
  const [selectedEvent, setSelectedEvent] = useState<Evenement | null>(null);
  const [selectedCoaching, setSelectedCoaching] = useState<Coaching | null>(
    null
  );
  const [loading, setLoading] = useState<boolean>(true);
  const [loadingUsers, setLoadingUsers] = useState<boolean>(false); // Nouvel état pour le chargement des utilisateurs
  const [adding, setAdding] = useState<boolean>(false);
  const [updating, setUpdating] = useState<boolean>(false);
  const [addingCoaching, setAddingCoaching] = useState<boolean>(false);
  const [updatingCoaching, setUpdatingCoaching] = useState<boolean>(false);
  const [error, setError] = useState<string>("");
  const [success, setSuccess] = useState<string>("");
  const [toast, setToast] = useState<{
    message: string;
    type: "success" | "error" | "info";
  } | null>(null);
  const [confirmModal, setConfirmModal] = useState<{
    isOpen: boolean;
    id: number | null;
    type: "evenement" | "coaching" | null;
    message: string;
  }>({
    isOpen: false,
    id: null,
    type: null,
    message: "",
  });
  const [filter, setFilter] = useState<"tous" | "evenements" | "coachings">(
    "tous"
  );

  const [coachSearch, setCoachSearch] = useState("");
  const [clientSearch, setClientSearch] = useState("");

  const navigate = useNavigate();
  const [currentDate, setCurrentDate] = useState(new Date());
  const [currentView, setCurrentView] = useState<View>(Views.MONTH);

  const [formData, setFormData] = useState<EvenementFormData>({
    nom: "",
    description: "",
    dateDebut: "",
    dateFin: "",
  });

  const [coachingFormData, setCoachingFormData] = useState<CoachingFormData>({
    nomCours: "",
    description: "",
    coachId: "",
    clientId: "",
    prix: "",
    dateDebut: "",
    dateFin: "",
  });

  const token = localStorage.getItem("token");

  useEffect(() => {
    if (!token) {
      navigate("/login");
      return;
    }
    fetchAllData();
  }, [token, navigate, currentDate]); // Ajout de currentDate pour recharger les données si la date change

  const fetchAllData = async () => {
    setLoading(true);
    try {
      await Promise.all([fetchEvenements(), fetchCoachings(), fetchUsers()]);
    } catch (error) {
      console.error("Erreur lors du chargement des données:", error);
    } finally {
      setLoading(false);
    }
  };

  const showToast = (message: string, type: "success" | "error" | "info") => {
    setToast({ message, type });
  };

  const closeToast = () => {
    setToast(null);
  };

  const openConfirmModal = (
    id: number,
    type: "evenement" | "coaching",
    message: string
  ) => {
    setConfirmModal({ isOpen: true, id, type, message });
  };

  const closeConfirmModal = () => {
    setConfirmModal({ isOpen: false, id: null, type: null, message: "" });
  };

  const openEventDetail = (event: Evenement) => {
    setSelectedEvent(event);
    setShowDetailModal(true);
  };

  const closeEventDetail = () => {
    setShowDetailModal(false);
    setSelectedEvent(null);
  };

  const openCoachingDetail = (coaching: Coaching) => {
    setSelectedCoaching(coaching);
    setShowCoachingDetailModal(true);
  };

  const closeCoachingDetail = () => {
    setShowCoachingDetailModal(false);
    setSelectedCoaching(null);
  };

  const handleLogout = () => {
    localStorage.removeItem("token");
    setIsLoggedIn(false);
    navigate("/login");
  };

  const fetchEvenements = async () => {
    try {
      const startOfMonth = moment(currentDate)
        .startOf("month")
        .format("YYYY-MM-DDTHH:mm:ss");
      const endOfMonth = moment(currentDate)
        .endOf("month")
        .format("YYYY-MM-DDTHH:mm:ss");
      const url = `http://localhost:8080/api/evenements/liste?start=${startOfMonth}&end=${endOfMonth}`;

      const response = await axios.get<Evenement[]>(url, {
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
      });

      setEvenements(response.data);
    } catch (err: any) {
      console.error("Erreur détaillée:", err.response?.data || err.message);
      setError(
        `Erreur lors de la récupération des événements: ${
          err.response?.data?.message || err.message
        }`
      );
    }
  };

  const fetchCoachings = async () => {
    try {
      const data = await fetchCoachingsAPI(token!);
      setCoachings(data);
    } catch (err: any) {
      console.error("Erreur lors de la récupération des coachings:", err);
      showToast(
        `Erreur lors de la récupération des coachings: ${err.message}`,
        "error"
      );
    }
  };

  const fetchUsers = async () => {
    setLoadingUsers(true);
    try {
      console.log("Récupération des utilisateurs..."); // Débogage
      const [coachesData, clientsData] = await Promise.all([
        fetchCoachesAPI(token!),
        fetchClientsAPI(token!),
      ]);
      console.log("Coaches récupérés:", coachesData); // Débogage
      console.log("Clients récupérés:", clientsData); // Débogage
      setCoaches(coachesData);
      setClients(clientsData);
    } catch (err: any) {
      const errorMessage =
        err.message || "Erreur lors de la récupération des utilisateurs";
      console.error("Erreur lors de la récupération des utilisateurs:", err);
      showToast(errorMessage, "error");
    } finally {
      setLoadingUsers(false);
    }
  };

  const fetchEvenementById = async (id: number) => {
    try {
      const response = await axios.get<Evenement>(
        `http://localhost:8080/api/evenements/getById/${id}`,
        {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        }
      );
      const evenement = response.data;
      setFormData({
        id: evenement.id,
        nom: evenement.nom,
        description: evenement.description,
        dateDebut: moment(evenement.dateDebut).format("YYYY-MM-DDTHH:mm"),
        dateFin: evenement.dateFin
          ? moment(evenement.dateFin).format("YYYY-MM-DDTHH:mm")
          : "",
      });
      setShowEditForm(true);
      setShowDetailModal(false);
      showToast("Événement chargé avec succès", "success");
    } catch (err: any) {
      const errorMessage = `Erreur lors de la récupération de l'événement: ${
        err.response?.data?.message || err.message
      }`;
      setError(errorMessage);
      showToast(errorMessage, "error");
    }
  };

  const prepareEditForm = (event: Evenement) => {
    setFormData({
      id: event.id,
      nom: event.nom,
      description: event.description,
      dateDebut: moment(event.dateDebut).format("YYYY-MM-DDTHH:mm"),
      dateFin: event.dateFin
        ? moment(event.dateFin).format("YYYY-MM-DDTHH:mm")
        : "",
    });
    setShowEditForm(true);
    setShowDetailModal(false);
  };

  const prepareEditCoachingForm = () => {
    if (selectedCoaching) {
      setCoachingFormData({
        id: selectedCoaching.id,
        nomCours: selectedCoaching.nomCours,
        description: selectedCoaching.description,
        coachId: selectedCoaching.coachId.toString(),
        clientId: selectedCoaching.clientId.toString(),
        prix: selectedCoaching.prix.toString(),
        dateDebut: moment(selectedCoaching.dateDebut).format(
          "YYYY-MM-DDTHH:mm"
        ),
        dateFin: selectedCoaching.dateFin
          ? moment(selectedCoaching.dateFin).format("YYYY-MM-DDTHH:mm")
          : "",
      });
      setShowEditCoachingForm(true);
      setShowCoachingDetailModal(false);
    }
  };

  const prepareDeleteCoaching = () => {
    if (selectedCoaching) {
      openConfirmModal(
        selectedCoaching.id,
        "coaching",
        `Voulez-vous vraiment supprimer la séance "${selectedCoaching.nomCours}" ? Cette action est irréversible.`
      );
    }
  };

  const handleAddEvenement = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!formData.nom || !formData.dateDebut) {
      const errorMessage = "Le nom et la date de début sont obligatoires.";
      setError(errorMessage);
      showToast(errorMessage, "error");
      return;
    }

    if (
      formData.dateFin &&
      new Date(formData.dateDebut) >= new Date(formData.dateFin)
    ) {
      const errorMessage =
        "La date de début doit être antérieure à la date de fin.";
      setError(errorMessage);
      showToast(errorMessage, "error");
      return;
    }

    setAdding(true);
    try {
      const formattedDateDebut = moment(formData.dateDebut).format(
        "YYYY-MM-DDTHH:mm:ss"
      );
      const formattedDateFin = formData.dateFin
        ? moment(formData.dateFin).format("YYYY-MM-DDTHH:mm:ss")
        : null;

      await axios.post(
        "http://localhost:8080/api/evenements/ajouter",
        {
          nom: formData.nom,
          description: formData.description,
          dateDebut: formattedDateDebut,
          dateFin: formattedDateFin,
        },
        {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        }
      );

      const successMessage = "Événement ajouté avec succès !";
      setSuccess(successMessage);
      showToast(successMessage, "success");
      setShowAddForm(false);
      setFormData({
        nom: "",
        description: "",
        dateDebut: "",
        dateFin: "",
      });
      fetchEvenements();
    } catch (err: any) {
      const errorMessage = `Erreur lors de l'ajout : ${
        err.response?.data?.message || err.message
      }`;
      setError(errorMessage);
      showToast(errorMessage, "error");
    } finally {
      setAdding(false);
    }
  };

  const handleUpdateEvenement = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!formData.nom || !formData.dateDebut) {
      const errorMessage = "Le nom et la date de début sont obligatoires.";
      setError(errorMessage);
      showToast(errorMessage, "error");
      return;
    }

    if (
      formData.dateFin &&
      new Date(formData.dateDebut) >= new Date(formData.dateFin)
    ) {
      const errorMessage =
        "La date de début doit être antérieure à la date de fin.";
      setError(errorMessage);
      showToast(errorMessage, "error");
      return;
    }

    setUpdating(true);
    try {
      const formattedDateDebut = moment(formData.dateDebut).format(
        "YYYY-MM-DDTHH:mm:ss"
      );
      const formattedDateFin = formData.dateFin
        ? moment(formData.dateFin).format("YYYY-MM-DDTHH:mm:ss")
        : null;

      await axios.put(
        `http://localhost:8080/api/evenements/mettre_a_jour/${formData.id}`,
        {
          nom: formData.nom,
          description: formData.description,
          dateDebut: formattedDateDebut,
          dateFin: formattedDateFin,
        },
        {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        }
      );

      const successMessage = "Événement mis à jour avec succès !";
      setSuccess(successMessage);
      showToast(successMessage, "success");
      setShowEditForm(false);
      setFormData({
        nom: "",
        description: "",
        dateDebut: "",
        dateFin: "",
      });
      fetchEvenements();
    } catch (err: any) {
      const errorMessage = `Erreur lors de la mise à jour : ${
        err.response?.data?.message || err.message
      }`;
      setError(errorMessage);
      showToast(errorMessage, "error");
    } finally {
      setUpdating(false);
    }
  };

  const handleAddCoaching = async (e: React.FormEvent) => {
    e.preventDefault();

    if (
      !coachingFormData.nomCours ||
      !coachingFormData.coachId ||
      !coachingFormData.clientId ||
      !coachingFormData.dateDebut
    ) {
      const errorMessage =
        "Le nom du cours, le coach, le client et la date de début sont obligatoires.";
      setError(errorMessage);
      showToast(errorMessage, "error");
      return;
    }

    if (
      coachingFormData.dateFin &&
      new Date(coachingFormData.dateDebut) >= new Date(coachingFormData.dateFin)
    ) {
      const errorMessage =
        "La date de début doit être antérieure à la date de fin.";
      setError(errorMessage);
      showToast(errorMessage, "error");
      return;
    }

    setAddingCoaching(true);
    try {
      await handleAddCoachingAPI(coachingFormData, token!);

      const successMessage = "Séance de coaching ajoutée avec succès !";
      setSuccess(successMessage);
      showToast(successMessage, "success");
      setShowAddCoachingForm(false);
      setCoachingFormData({
        nomCours: "",
        description: "",
        coachId: "",
        clientId: "",
        prix: "",
        dateDebut: "",
        dateFin: "",
      });
      fetchCoachings();
    } catch (err: any) {
      const errorMessage = `Erreur lors de l'ajout : ${err.message}`;
      setError(errorMessage);
      showToast(errorMessage, "error");
    } finally {
      setAddingCoaching(false);
    }
  };

  const handleUpdateCoaching = async (e: React.FormEvent) => {
    e.preventDefault();

    if (
      !coachingFormData.nomCours ||
      !coachingFormData.coachId ||
      !coachingFormData.clientId ||
      !coachingFormData.dateDebut
    ) {
      const errorMessage =
        "Le nom du cours, le coach, le client et la date de début sont obligatoires.";
      setError(errorMessage);
      showToast(errorMessage, "error");
      return;
    }

    if (
      coachingFormData.dateFin &&
      new Date(coachingFormData.dateDebut) >= new Date(coachingFormData.dateFin)
    ) {
      const errorMessage =
        "La date de début doit être antérieure à la date de fin.";
      setError(errorMessage);
      showToast(errorMessage, "error");
      return;
    }

    setUpdatingCoaching(true);
    try {
      await handleUpdateCoachingAPI(
        coachingFormData.id!,
        coachingFormData,
        token!
      );

      const successMessage = "Séance de coaching mise à jour avec succès !";
      setSuccess(successMessage);
      showToast(successMessage, "success");
      setShowEditCoachingForm(false);
      setCoachingFormData({
        nomCours: "",
        description: "",
        coachId: "",
        clientId: "",
        prix: "",
        dateDebut: "",
        dateFin: "",
      });
      fetchCoachings();
    } catch (err: any) {
      const errorMessage = `Erreur lors de la mise à jour : ${err.message}`;
      setError(errorMessage);
      showToast(errorMessage, "error");
    } finally {
      setUpdatingCoaching(false);
    }
  };

  const handleDeleteEvenement = async (id: number) => {
    try {
      await axios.delete(
        `http://localhost:8080/api/evenements/supprimer/${id}`,
        {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        }
      );

      const successMessage = "Événement supprimé avec succès !";
      setSuccess(successMessage);
      showToast(successMessage, "success");
      fetchEvenements();
      setShowDetailModal(false);
    } catch (err: any) {
      const errorMessage = `Erreur lors de la suppression : ${
        err.response?.data?.message || err.message
      }`;
      setError(errorMessage);
      showToast(errorMessage, "error");
    }
  };

  const handleDeleteCoaching = async (id: number) => {
    try {
      await handleDeleteCoachingAPI(id, token!);

      const successMessage = "Séance de coaching supprimée avec succès !";
      setSuccess(successMessage);
      showToast(successMessage, "success");
      fetchCoachings();
      setShowCoachingDetailModal(false);
    } catch (err: any) {
      const errorMessage = `Erreur lors de la suppression : ${err.message}`;
      setError(errorMessage);
      showToast(errorMessage, "error");
    }
  };

  const confirmDelete = () => {
    if (confirmModal.id && confirmModal.type) {
      if (confirmModal.type === "evenement") {
        handleDeleteEvenement(confirmModal.id);
      } else {
        handleDeleteCoaching(confirmModal.id);
      }
    }
    closeConfirmModal();
  };

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
  ) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleCoachingChange = (
    e: React.ChangeEvent<
      HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement
    >
  ) => {
    const { name, value } = e.target;
    setCoachingFormData((prev) => ({ ...prev, [name]: value }));
  };

  const closeAddForm = () => {
    setShowAddForm(false);
    setFormData({
      nom: "",
      description: "",
      dateDebut: "",
      dateFin: "",
    });
    setError("");
  };

  const closeEditForm = () => {
    setShowEditForm(false);
    setFormData({
      nom: "",
      description: "",
      dateDebut: "",
      dateFin: "",
    });
    setError("");
  };

  const closeAddCoachingForm = () => {
    setShowAddCoachingForm(false);
    setCoachingFormData({
      nomCours: "",
      description: "",
      coachId: "",
      clientId: "",
      prix: "",
      dateDebut: "",
      dateFin: "",
    });
    setError("");
  };

  const closeEditCoachingForm = () => {
    setShowEditCoachingForm(false);
    setCoachingFormData({
      nomCours: "",
      description: "",
      coachId: "",
      clientId: "",
      prix: "",
      dateDebut: "",
      dateFin: "",
    });
    setError("");
  };

  const handleNavigate = (newDate: Date) => {
    setCurrentDate(newDate);
    fetchAllData();
  };

  const handleView = (newView: View) => {
    setCurrentView(newView);
  };

  const formats = {
    dateFormat: "D",
    dayFormat: (date: Date, culture?: string, localizer?: any) =>
      localizer.format(date, "DD", culture),
    dayRangeHeaderFormat: (
      { start, end }: { start: Date; end: Date },
      culture?: string,
      localizer?: any
    ) =>
      `${localizer.format(start, "DD MMMM", culture)} - ${localizer.format(
        end,
        "DD MMMM",
        culture
      )}`,
    monthHeaderFormat: (date: Date, culture?: string, localizer?: any) =>
      localizer.format(date, "MMMM YYYY", culture),
    dayHeaderFormat: (date: Date, culture?: string, localizer?: any) =>
      localizer.format(date, "dddd DD MMMM", culture),
    agendaHeaderFormat: (
      { start, end }: { start: Date; end: Date },
      culture?: string,
      localizer?: any
    ) =>
      `${localizer.format(start, "DD MMMM", culture)} - ${localizer.format(
        end,
        "DD MMMM",
        culture
      )}`,
    agendaTimeFormat: (date: Date, culture?: string, localizer?: any) =>
      localizer.format(date, "HH:mm", culture),
    agendaTimeRangeFormat: (
      { start, end }: { start: Date; end: Date },
      culture?: string,
      localizer?: any
    ) =>
      `${localizer.format(start, "HH:mm", culture)} - ${localizer.format(
        end,
        "HH:mm",
        culture
      )}`,
    timeGutterFormat: (date: Date, culture?: string, localizer?: any) =>
      localizer.format(date, "HH:mm", culture),
  };

  const calendarEvents = [
    // Événements
    ...evenements
      .map((event) => {
        try {
          const startDate = new Date(event.dateDebut);
          const endDate = event.dateFin
            ? new Date(event.dateFin)
            : new Date(event.dateDebut);

          if (isNaN(startDate.getTime())) {
            console.error("Date de début invalide:", event.dateDebut);
            return null;
          }
          if (event.dateFin && isNaN(endDate.getTime())) {
            console.error("Date de fin invalide:", event.dateFin);
            return null;
          }

          return {
            id: event.id,
            title: event.nom,
            start: startDate,
            end: endDate,
            allDay:
              !event.dateFin ||
              moment(event.dateDebut).isSame(event.dateFin, "day"),
            resource: {
              type: "evenement",
              description: event.description,
              statut: event.statutEvent,
              createdBy: event.createdByName,
            },
          };
        } catch (error) {
          console.error("Erreur de conversion de date:", error);
          return null;
        }
      })
      .filter((event): event is NonNullable<typeof event> => event !== null),

    // Coachings
    ...coachings
      .map((coaching) => {
        try {
          const startDate = new Date(coaching.dateDebut);
          const endDate = coaching.dateFin
            ? new Date(coaching.dateFin)
            : new Date(coaching.dateDebut);

          if (isNaN(startDate.getTime())) {
            console.error(
              "Date de début invalide (coaching):",
              coaching.dateDebut
            );
            return null;
          }
          if (coaching.dateFin && isNaN(endDate.getTime())) {
            console.error("Date de fin invalide (coaching):", coaching.dateFin);
            return null;
          }

          return {
            id: coaching.id,
            title: `Coaching: ${coaching.nomCours}`,
            start: startDate,
            end: endDate,
            allDay:
              !coaching.dateFin ||
              moment(coaching.dateDebut).isSame(coaching.dateFin, "day"),
            resource: {
              type: "coaching",
              description: coaching.description,
              client: coaching.nomClient,
              coach: coaching.nomCoach,
              prix: coaching.prix,
            },
          };
        } catch (error) {
          console.error("Erreur de conversion de date (coaching):", error);
          return null;
        }
      })
      .filter((event): event is NonNullable<typeof event> => event !== null),
  ];

  const filteredCalendarEvents = calendarEvents.filter((event) => {
    if (filter === "tous") return true;
    if (filter === "evenements") return event.resource.type === "evenement";
    if (filter === "coachings") return event.resource.type === "coaching";
    return true;
  });

  const filteredList =
    filter === "tous"
      ? [...evenements, ...coachings]
      : filter === "evenements"
      ? evenements
      : coachings;

  const eventStyleGetter = (event: any) => {
    let backgroundColor = "#3174ad"; // Bleu par défaut

    if (event.resource.type === "coaching") {
      backgroundColor = "#9333ea"; // Violet pour les coachings
    } else {
      switch (event.resource.statut) {
        case "EN_ATTENTE":
          backgroundColor = "#ffc107"; // Jaune
          break;
        case "EN_COURS":
          backgroundColor = "#28a745"; // Vert
          break;
        case "TERMINER":
          backgroundColor = "#6c757d"; // Gris
          break;
      }
    }

    return {
      style: {
        backgroundColor,
        borderRadius: "5px",
        opacity: 0.8,
        color: "white",
        border: "0px",
        display: "block",
      },
    };
  };

  useEffect(() => {
    if (token) {
      fetchAllData();
    }
  }, [token, currentDate]);

  if (loading) {
    return (
      <div className="min-h-screen bg-black flex items-center justify-center">
        <div className="text-white text-xl">Chargement des données...</div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-b from-black via-black to-orange-500 flex flex-col">
      {/* Toast notifications */}
      {toast && (
        <Toast message={toast.message} type={toast.type} onClose={closeToast} />
      )}

      {/* Modal de confirmation pour suppression */}
      <ConfirmModal
        isOpen={confirmModal.isOpen}
        onClose={closeConfirmModal}
        onConfirm={confirmDelete}
        message={confirmModal.message}
      />

      {/* Modal de détail de l'événement */}
      <EventDetailModal
        isOpen={showDetailModal}
        onClose={closeEventDetail}
        event={selectedEvent}
        onEdit={() => selectedEvent && prepareEditForm(selectedEvent)}
        onDelete={() =>
          selectedEvent &&
          openConfirmModal(
            selectedEvent.id,
            "evenement",
            `Voulez-vous vraiment supprimer l'événement "${selectedEvent.nom}" ? Cette action est irréversible.`
          )
        }
      />

      {/* Modal de détail du coaching */}
      <CoachingDetailModal
        isOpen={showCoachingDetailModal}
        onClose={closeCoachingDetail}
        coaching={selectedCoaching}
        onEdit={prepareEditCoachingForm}
        onDelete={prepareDeleteCoaching}
      />

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
              Gestion des Événements et Coachings
            </h1>
            <p className="text-gray-400">
              {evenements.length} événement{evenements.length !== 1 ? "s" : ""}{" "}
              et {coachings.length} séance{coachings.length !== 1 ? "s" : ""} de
              coaching
            </p>
          </div>
          <div className="flex space-x-3">
            <button
              onClick={() => setShowAddForm(true)}
              className="bg-orange-500 font-bold text-white  px-4 py-2 rounded-lg hover:bg-white hover:text-orange-500 transition-colors"
            >
              + Ajouter Event
            </button>
            <button
              onClick={() => setShowAddCoachingForm(true)}
              className="bg-purple-500 font-bold text-white px-4 py-2 rounded-lg hover:bg-white hover:text-purple-500 transition-colors"
            >
              + Ajouter Coaching
            </button>
          </div>
        </div>

        {/* Filtre */}
        <div className="mb-6">
          <label className="block text-white font-semibold mb-2">
            Filtrer par type :
          </label>
          <select
            value={filter}
            onChange={(e) =>
              setFilter(e.target.value as "tous" | "evenements" | "coachings")
            }
            className="w-48 p-2 border rounded"
          >
            <option value="tous">Tous</option>
            <option value="evenements">Événements</option>
            <option value="coachings">Coachings</option>
          </select>
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

        {/* Legende */}

        <div className="bg-white p-4 rounded-lg shadow-lg mb-6">
          <h3 className="font-semibold mb-3">Légende :</h3>
          <div className="flex flex-wrap gap-4">
            <div className="flex items-center">
              <div className="w-4 h-4 bg-yellow-400 rounded mr-2"></div>
              <span className="text-sm">En attente</span>
            </div>
            <div className="flex items-center">
              <div className="w-4 h-4 bg-green-500 rounded mr-2"></div>
              <span className="text-sm">En cours</span>
            </div>
            <div className="flex items-center">
              <div className="w-4 h-4 bg-gray-500 rounded mr-2"></div>
              <span className="text-sm">Terminé</span>
            </div>
            <div className="flex items-center">
              <div className="w-4 h-4 bg-purple-500 rounded mr-2"></div>
              <span className="text-sm">Coaching</span>
            </div>
            <div className="flex items-center">
              <div className="w-4 h-4 bg-blue-500 rounded mr-2"></div>
              <span className="text-sm">Non spécifié</span>
            </div>
          </div>
        </div>

        {/* Calendrier */}

        <div className="bg-white rounded-lg shadow-lg p-6">
          <Calendar
            localizer={localizer}
            events={filteredCalendarEvents}
            startAccessor="start"
            endAccessor="end"
            style={{ height: 600 }}
            eventPropGetter={eventStyleGetter}
            messages={frenchMessages}
            formats={formats}
            date={currentDate}
            view={currentView}
            onNavigate={handleNavigate}
            onView={handleView}
            culture="fr"
            defaultView={Views.MONTH}
            step={60}
            timeslots={2}
            showMultiDayTimes
            selectable
            popup
            onSelectEvent={(calendarEvent) => {
              if (calendarEvent.resource.type === "coaching") {
                const coaching = coachings.find(
                  (c) => c.id === calendarEvent.id
                );
                if (coaching) {
                  openCoachingDetail(coaching);
                }
              } else if (calendarEvent.resource.type === "evenement") {
                const event = evenements.find((e) => e.id === calendarEvent.id);
                if (event) {
                  openEventDetail(event);
                }
              }
            }}
          />
        </div>

        {/* Liste des evenements */}

        <div className="bg-white rounded-lg shadow-lg p-6 mt-6">
          <h3 className="text-xl font-semibold mb-4">
            Liste des Événements et Coachings
          </h3>
          {filteredList.length === 0 ? (
            <p className="text-gray-500">Aucun élément trouvé.</p>
          ) : (
            <div className="space-y-3">
              {filteredList.map((item) => (
                <div
                  key={item.id}
                  className="border rounded-lg p-4 hover:bg-gray-50 cursor-pointer"
                  onClick={() => {
                    if ("statutEvent" in item) {
                      openEventDetail(item as Evenement);
                    } else {
                      openCoachingDetail(item as Coaching);
                    }
                  }}
                >
                  <div className="flex justify-between items-start">
                    <div>
                      <h4 className="font-semibold">
                        {"statutEvent" in item
                          ? (item as Evenement).nom
                          : (item as Coaching).nomCours}
                      </h4>
                      <p className="text-sm text-gray-600">
                        {(item as Evenement | Coaching).description}
                      </p>
                      <p className="text-sm text-gray-500">
                        Du{" "}
                        {moment(
                          (item as Evenement | Coaching).dateDebut
                        ).format("DD/MM/YYYY HH:mm")}
                        {(item as Evenement | Coaching).dateFin &&
                          ` au ${moment(
                            (item as Evenement | Coaching).dateFin
                          ).format("DD/MM/YYYY HH:mm")}`}
                      </p>
                      <span
                        className={`inline-block px-2 py-1 text-xs rounded-full ${
                          "statutEvent" in item
                            ? (item as Evenement).statutEvent === "EN_ATTENTE"
                              ? "bg-yellow-100 text-yellow-800"
                              : (item as Evenement).statutEvent === "EN_COURS"
                              ? "bg-green-100 text-green-800"
                              : "bg-gray-100 text-gray-800"
                            : "bg-purple-100 text-purple-800"
                        }`}
                      >
                        {"statutEvent" in item
                          ? (item as Evenement).statutEvent
                          : "Coaching"}
                      </span>
                    </div>
                    <div className="flex space-x-2">
                      <button
                        onClick={(e) => {
                          e.stopPropagation();
                          if ("statutEvent" in item) {
                            openConfirmModal(
                              item.id,
                              "evenement",
                              `Voulez-vous vraiment supprimer l'événement "${
                                (item as Evenement).nom
                              }" ? Cette action est irréversible.`
                            );
                          } else {
                            openConfirmModal(
                              item.id,
                              "coaching",
                              `Voulez-vous vraiment supprimer la séance "${
                                (item as Coaching).nomCours
                              }" ? Cette action est irréversible.`
                            );
                          }
                        }}
                        className="bg-red-600 text-white rounded-full px-3 py-1 rounded hover:bg-red-600 transition-colors"
                      >
                        Supprimer
                      </button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Modal d'ajout d'événement */}
        {showAddForm && (
          <div
            className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
            onClick={(e) => {
              const modal = e.currentTarget.querySelector(".bg-white");
              if (modal && !modal.contains(e.target as Node)) {
                closeAddForm();
              }
            }}
          >
            <div className="bg-white rounded-lg p-6 w-full max-w-md">
              <div className="flex justify-between items-center mb-4">
                <h2 className="text-2xl font-bold">Ajouter un Événement</h2>
                <button
                  onClick={closeAddForm}
                  className="text-gray-500 hover:text-gray-700 text-2xl"
                >
                  ×
                </button>
              </div>

              <form onSubmit={handleAddEvenement} className="space-y-4">
                <div>
                  <label className="block text-gray-700">
                    Nom de l'événement *
                  </label>
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
                  <label className="block text-gray-700">Description</label>
                  <textarea
                    name="description"
                    value={formData.description}
                    onChange={handleChange}
                    rows={3}
                    className="w-full p-2 border rounded"
                  />
                </div>

                <div>
                  <label className="block text-gray-700">Date de début *</label>
                  <input
                    type="datetime-local"
                    name="dateDebut"
                    value={formData.dateDebut}
                    onChange={handleChange}
                    className="w-full p-2 border rounded"
                    required
                  />
                </div>

                <div>
                  <label className="block text-gray-700">Date de fin</label>
                  <input
                    type="datetime-local"
                    name="dateFin"
                    value={formData.dateFin}
                    onChange={handleChange}
                    className="w-full p-2 border rounded"
                  />
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

        {/* Modal d'ajout de coaching */}
        {showAddCoachingForm && (
          <div
            className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
            onClick={(e) => {
              const modal = e.currentTarget.querySelector(".bg-white");
              if (modal && !modal.contains(e.target as Node)) {
                closeAddCoachingForm();
              }
            }}
          >
            <div className="bg-white rounded-lg p-6 w-full max-w-md">
              <div className="flex justify-between items-center mb-4">
                <h2 className="text-2xl font-bold">
                  Ajouter une Séance de Coaching
                </h2>
                <button
                  onClick={closeAddCoachingForm}
                  className="text-gray-500 hover:text-gray-700 text-2xl"
                >
                  ×
                </button>
              </div>

              <form onSubmit={handleAddCoaching} className="space-y-2">
                <div>
                  <label className="block text-gray-700">Nom du cours *</label>
                  <input
                    type="text"
                    name="nomCours"
                    value={coachingFormData.nomCours}
                    onChange={handleCoachingChange}
                    className="w-full p-2 border rounded"
                    required
                  />
                </div>

                <div>
                  <label className="block text-gray-700">Description</label>
                  <textarea
                    name="description"
                    value={coachingFormData.description}
                    onChange={handleCoachingChange}
                    rows={3}
                    className="w-full p-2 border rounded"
                  />
                </div>

                <div>
                  <label className="block text-gray-700">Coach *</label>
                  <input
                    type="text"
                    value={coachSearch}
                    onChange={(e) => setCoachSearch(e.target.value)}
                    placeholder="Rechercher un coach..."
                    className="w-full p-2 border rounded mb-2"
                  />
                  <ul className="border rounded max-h-40 overflow-y-auto">
                    {coaches
                      .filter((coach) =>
                        `${coach.nom} ${coach.prenom} ${coach.telephone}`
                          .toLowerCase()
                          .includes(coachSearch.toLowerCase())
                      )
                      .map((coach) => (
                        <li
                          key={coach.id}
                          onClick={() => {
                            setCoachingFormData((prev) => ({
                              ...prev,
                              coachId: coach.id.toString(),
                            }));
                            setCoachSearch(
                              `${coach.nom} ${coach.prenom} - ${coach.telephone}`
                            );
                          }}
                          className="p-2 hover:bg-gray-100 cursor-pointer"
                        >
                          {coach.nom} {coach.prenom} - {coach.telephone}
                        </li>
                      ))}
                  </ul>
                </div>

                <div>
                  <label className="block text-gray-700">Client *</label>
                  <input
                    type="text"
                    value={clientSearch}
                    onChange={(e) => setClientSearch(e.target.value)}
                    placeholder="Rechercher un client..."
                    className="w-full p-2 border rounded mb-2"
                  />
                  <ul className="border rounded max-h-40 overflow-y-auto">
                    {clients
                      .filter((client) =>
                        `${client.nom} ${client.prenom} ${client.telephone}`
                          .toLowerCase()
                          .includes(clientSearch.toLowerCase())
                      )
                      .map((client) => (
                        <li
                          key={client.id}
                          onClick={() => {
                            setCoachingFormData((prev) => ({
                              ...prev,
                              clientId: client.id.toString(),
                            }));
                            setClientSearch(
                              `${client.nom} ${client.prenom} - ${client.telephone}`
                            );
                          }}
                          className="p-2 hover:bg-gray-100 cursor-pointer"
                        >
                          {client.nom} {client.prenom} - {client.telephone}
                        </li>
                      ))}
                  </ul>
                </div>

                <div>
                  <label className="block text-gray-700">Prix *</label>
                  <input
                    type="number"
                    name="prix"
                    value={coachingFormData.prix}
                    onChange={handleCoachingChange}
                    className="w-full p-2 border rounded"
                    step="0.01"
                    min="0"
                    required
                  />
                </div>

                <div>
                  <label className="block text-gray-700">Date de début *</label>
                  <input
                    type="datetime-local"
                    name="dateDebut"
                    value={coachingFormData.dateDebut}
                    onChange={handleCoachingChange}
                    className="w-full p-2 border rounded"
                    required
                  />
                </div>

                <div>
                  <label className="block text-gray-700">Date de fin</label>
                  <input
                    type="datetime-local"
                    name="dateFin"
                    value={coachingFormData.dateFin}
                    onChange={handleCoachingChange}
                    className="w-full p-2 border rounded"
                  />
                </div>

                <div className="flex justify-end space-x-3 mt-6">
                  <button
                    type="button"
                    onClick={closeAddCoachingForm}
                    className="bg-black text-orange-500 px-4 py-2 rounded hover:bg-gray-600 transition-colors"
                  >
                    Annuler
                  </button>
                  <button
                    type="submit"
                    disabled={addingCoaching}
                    className="bg-orange-500 text-white px-6 py-2 rounded-lg hover:bg-orange-400 transition-colors font-bold"
                  >
                    {addingCoaching ? "Ajout en cours..." : "Ajouter"}
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}
        {/* Modal d'édition d'événement */}
        {showEditForm && (
          <div
            className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
            onClick={(e) => {
              const modal = e.currentTarget.querySelector(".bg-white");
              if (modal && !modal.contains(e.target as Node)) {
                closeEditForm();
              }
            }}
          >
            <div className="bg-white rounded-lg p-6 w-full max-w-md">
              <div className="flex justify-between items-center mb-4">
                <h2 className="text-2xl font-bold">Modifier un Événement</h2>
                <button
                  onClick={closeEditForm}
                  className="text-gray-500 hover:text-gray-700 text-2xl"
                >
                  ×
                </button>
              </div>

              <form onSubmit={handleUpdateEvenement} className="space-y-4">
                <div>
                  <label className="block text-gray-700">
                    Nom de l'événement *
                  </label>
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
                  <label className="block text-gray-700">Description</label>
                  <textarea
                    name="description"
                    value={formData.description}
                    onChange={handleChange}
                    rows={3}
                    className="w-full p-2 border rounded"
                  />
                </div>

                <div>
                  <label className="block text-gray-700">Date de début *</label>
                  <input
                    type="datetime-local"
                    name="dateDebut"
                    value={formData.dateDebut}
                    onChange={handleChange}
                    className="w-full p-2 border rounded"
                    required
                  />
                </div>

                <div>
                  <label className="block text-gray-700">Date de fin</label>
                  <input
                    type="datetime-local"
                    name="dateFin"
                    value={formData.dateFin}
                    onChange={handleChange}
                    className="w-full p-2 border rounded"
                  />
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
                    {updating ? "Mise à jour en cours..." : "Mettre à jour"}
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}

        {/* Modal d'édition de coaching */}
        {showEditCoachingForm && (
          <div
            className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
            onClick={(e) => {
              const modal = e.currentTarget.querySelector(".bg-white");
              if (modal && !modal.contains(e.target as Node)) {
                closeEditCoachingForm();
              }
            }}
          >
            <div className="bg-white rounded-lg p-6 w-full max-w-md">
              <div className="flex justify-between items-center mb-4">
                <h2 className="text-2xl font-bold">
                  Modifier une Séance de Coaching
                </h2>
                <button
                  onClick={closeEditCoachingForm}
                  className="text-gray-500 hover:text-gray-700 text-2xl"
                >
                  ×
                </button>
              </div>

              <form onSubmit={handleUpdateCoaching} className="space-y-4">
                <div>
                  <label className="block text-gray-700">Nom du cours *</label>
                  <input
                    type="text"
                    name="nomCours"
                    value={coachingFormData.nomCours}
                    onChange={handleCoachingChange}
                    className="w-full p-2 border rounded"
                    required
                  />
                </div>

                <div>
                  <label className="block text-gray-700">Description</label>
                  <textarea
                    name="description"
                    value={coachingFormData.description}
                    onChange={handleCoachingChange}
                    rows={3}
                    className="w-full p-2 border rounded"
                  />
                </div>

                <div>
                  <label className="block text-gray-700">Coach *</label>
                  <select
                    name="coachId"
                    value={coachingFormData.coachId}
                    onChange={handleCoachingChange}
                    className="w-full p-2 border rounded"
                    required
                  >
                    <option value="">Sélectionnez un coach</option>
                    {coaches.map((coach) => (
                      <option key={coach.id} value={coach.id}>
                        {coach.nom} {coach.prenom} - {coach.telephone}
                      </option>
                    ))}
                  </select>
                </div>

                <div>
                  <label className="block text-gray-700">Client *</label>
                  <select
                    name="clientId"
                    value={coachingFormData.clientId}
                    onChange={handleCoachingChange}
                    className="w-full p-2 border rounded"
                    required
                  >
                    <option value="">Sélectionnez un client</option>
                    {clients.map((client) => (
                      <option key={client.id} value={client.id}>
                        {client.nom} {client.prenom} - {client.telephone}
                      </option>
                    ))}
                  </select>
                </div>

                <div>
                  <label className="block text-gray-700">Prix *</label>
                  <input
                    type="number"
                    name="prix"
                    value={coachingFormData.prix}
                    onChange={handleCoachingChange}
                    className="w-full p-2 border rounded"
                    step="0.01"
                    min="0"
                    required
                  />
                </div>

                <div>
                  <label className="block text-gray-700">Date de début *</label>
                  <input
                    type="datetime-local"
                    name="dateDebut"
                    value={coachingFormData.dateDebut}
                    onChange={handleCoachingChange}
                    className="w-full p-2 border rounded"
                    required
                  />
                </div>

                <div>
                  <label className="block text-gray-700">Date de fin</label>
                  <input
                    type="datetime-local"
                    name="dateFin"
                    value={coachingFormData.dateFin}
                    onChange={handleCoachingChange}
                    className="w-full p-2 border rounded"
                  />
                </div>

                <div className="flex justify-end space-x-3 mt-6">
                  <button
                    type="button"
                    onClick={closeEditCoachingForm}
                    className="bg-black text-orange-500 px-4 py-2 rounded hover:bg-gray-600 transition-colors"
                  >
                    Annuler
                  </button>
                  <button
                    type="submit"
                    disabled={updatingCoaching}
                    className="bg-orange-500 text-white px-6 py-2 rounded-lg hover:bg-orange-400 transition-colors font-bold"
                  >
                    {updatingCoaching
                      ? "Mise à jour en cours..."
                      : "Mettre à jour"}
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}

        {/* Styles CSS pour l'animation du toast */}
        <style>{`
          @keyframes slide-in {
            from {
              transform: translateX(100%);
              opacity: 0;
            }
            to {
              transform: translateX(0);
              opacity: 1;
            }
          }
          .animate-slide-in {
            animation: slide-in 0.3s ease-out;
          }
        `}</style>
      </main>
    </div>
  );
}

export default Evenement;
