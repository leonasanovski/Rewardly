export type TaskStatus = 'OPEN' | 'IN_PROGRESS' | 'DONE' | 'REVIEWED';

export type TaskCategory =
  | 'SPORT_AND_ACTIVITY'
  | 'STUDY_AND_LEARNING'
  | 'CHORES'
  | 'HEALTH_AND_HYGIENE'
  | 'CREATIVE'
  | 'SOCIAL_AND_FAMILY'
  | 'DIGITAL_WELLBEING';

export const TASK_CATEGORY_LABELS: Record<TaskCategory, string> = {
  SPORT_AND_ACTIVITY: 'Sport & Activity',
  STUDY_AND_LEARNING: 'Study & Learning',
  CHORES: 'Chores',
  HEALTH_AND_HYGIENE: 'Health & Hygiene',
  CREATIVE: 'Creative',
  SOCIAL_AND_FAMILY: 'Social & Family',
  DIGITAL_WELLBEING: 'Digital Wellbeing'
};

export interface TaskAttachment {
  id: string;
  fileName: string;
  filePath: string;
  fileType: string;
}

export interface Task {
  id: string;
  title: string;
  description: string;
  category: TaskCategory;
  tokenValue: number;
  dueDateNote: string;
  status: TaskStatus;
  assignedTo: { id: string; fullName: string; username: string };
  startedAt?: string;
  finishedAt?: string;
  childNote?: string;
  childSelfRating?: number;
  parentRating?: number;
  parentComment?: string;
  tokensEarned?: number;
}

export interface CreateTaskRequest {
  title: string;
  description: string;
  category: TaskCategory;
  tokenValue: number;
  dueDateNote: string;
  assignToAll: boolean;
  assignedToId?: string;
}
