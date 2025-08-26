// Coaching.tsx
import axios from "axios";

export interface Coaching {
  id: number;
  gymId: number;
  clientId: number;
  nomClient: string;
  coachId: number;
  nomCoach: string;
  dateDebut: string;
  dateFin: string;
  prix: number;
  nomCours: string;
  description: string;
}

export interface CoachingFormData {
  id?: number;
  nomCours: string;
  description: string;
  coachId: string;
  clientId: string;
  prix: string;
  dateDebut: string;
  dateFin: string;
}

export interface User {
  id: number;
  nom: string;
  prenom: string;
  telephone: string;
  role: string;
}

// Tout les sceances de coaching
export const fetchCoachings = async (token: string): Promise<Coaching[]> => {
  try {
    const response = await axios.get<Coaching[]>(
      "http://localhost:8080/api/coachings/liste",
      {
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
      }
    );
    return response.data;
  } catch (err: any) {
    throw new Error(
      err.response?.data?.message ||
        "Erreur lors de la récupération des coachings"
    );
  }
};

// Une seul sceance de coaching
export const fetchCoachingById = async (
  id: number,
  token: string
): Promise<Coaching> => {
  try {
    const response = await axios.get<Coaching>(
      `http://localhost:8080/api/coachings/${id}`,
      {
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
      }
    );
    return response.data;
  } catch (err: any) {
    throw new Error(
      err.response?.data?.message ||
        "Erreur lors de la récupération du coaching"
    );
  }
};

// Créer une sceance de coaching
export const handleAddCoaching = async (
  formData: CoachingFormData,
  token: string
): Promise<void> => {
  try {
    await axios.post(
      "http://localhost:8080/api/coachings/ajouter",
      {
        nomCours: formData.nomCours,
        description: formData.description,
        coachId: parseInt(formData.coachId),
        clientId: parseInt(formData.clientId),
        prix: parseFloat(formData.prix),
        dateDebut: formData.dateDebut,
        dateFin: formData.dateFin || null,
      },
      {
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
      }
    );
  } catch (err: any) {
    throw new Error(
      err.response?.data?.message || "Erreur lors de l'ajout du coaching"
    );
  }
};

// Mettre à jour une sceance de coaching
export const handleUpdateCoaching = async (
  id: number,
  formData: CoachingFormData,
  token: string
): Promise<void> => {
  try {
    await axios.put(
      `http://localhost:8080/api/coachings/mettre_a_jour/${id}`,
      {
        nomCours: formData.nomCours,
        description: formData.description,
        coachId: parseInt(formData.coachId),
        clientId: parseInt(formData.clientId),
        prix: parseFloat(formData.prix),
        dateDebut: formData.dateDebut,
        dateFin: formData.dateFin || null,
      },
      {
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
      }
    );
  } catch (err: any) {
    throw new Error(
      err.response?.data?.message || "Erreur lors de la mise à jour du coaching"
    );
  }
};

// Supprimer une sceance de coaching
export const handleDeleteCoaching = async (
  id: number,
  token: string
): Promise<void> => {
  try {
    await axios.delete(`http://localhost:8080/api/coachings/${id}`, {
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
      },
    });
  } catch (err: any) {
    throw new Error(
      err.response?.data?.message || "Erreur lors de la suppression du coaching"
    );
  }
};

// Récupérer tous les utilisateurs et filtrer les coachs (rôle = COACH)
export const fetchCoaches = async (token: string): Promise<User[]> => {
  try {
    const response = await axios.get<User[]>(
      "http://localhost:8080/api/users/staff",
      {
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
      }
    );
    // Filtrer les utilisateurs avec le rôle COACH
    return response.data.filter((user) => user.role === "COACH");
  } catch (err: any) {
    throw new Error(
      err.response?.data?.message || "Erreur lors de la récupération des coachs"
    );
  }
};

// Récupérer tous les utilisateurs et filtrer les clients (rôle = MEMBRE)
export const fetchClients = async (token: string): Promise<User[]> => {
  try {
    const response = await axios.get<User[]>(
      "http://localhost:8080/api/users/membre",
      {
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
      }
    );
    // Filtrer les utilisateurs avec le rôle MEMBRE
    return response.data.filter(
      (user) => user.role === "MEMBRE" || user.role === "MEMBRE_TEMPORAIRE"
    );
  } catch (err: any) {
    throw new Error(
      err.response?.data?.message ||
        "Erreur lors de la récupération des clients"
    );
  }
};
