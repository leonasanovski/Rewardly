import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { forkJoin } from 'rxjs';
import { TaskService } from '../../../core/services/task.service';
import { ChildService } from '../../../core/services/child.service';
import { Task, TASK_CATEGORY_LABELS, TaskCategory } from '../../../core/models/task.model';
import { Child } from '../../../core/models/child.model';
import { ParentNavComponent } from '../../../shared/components/parent-nav/parent-nav.component';

const STATUS_ORDER: Record<string, number> = { DONE: 0, IN_PROGRESS: 1, OPEN: 2, REVIEWED: 3 };

@Component({
  selector: 'app-task-management',
  standalone: true,
  imports: [CommonModule, FormsModule, ParentNavComponent],
  templateUrl: './task-management.component.html',
  styleUrl: './task-management.component.scss'
})
export class TaskManagementComponent implements OnInit {
  tasks: Task[] = [];
  children: Child[] = [];
  loading = true;
  error = '';

  filterChildId = '';
  filterStatus = '';

  showCreateModal = false;
  creating = false;
  createError = '';
  createForm = this.emptyForm();

  showDetailModal = false;
  detailTask: Task | null = null;

  readonly categoryLabels = TASK_CATEGORY_LABELS;
  readonly categories = Object.keys(TASK_CATEGORY_LABELS) as TaskCategory[];
  readonly allStatuses = ['OPEN', 'IN_PROGRESS', 'DONE', 'REVIEWED'];

  constructor(
    private taskService: TaskService,
    private childService: ChildService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.load();
  }

  private load(): void {
    this.loading = true;
    forkJoin({
      tasks: this.taskService.getTasks(),
      children: this.childService.getChildren()
    }).subscribe({
      next: ({ tasks, children }) => {
        this.tasks = tasks;
        this.children = children;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load tasks.';
        this.loading = false;
      }
    });
  }

  get filteredTasks(): Task[] {
    let list = [...this.tasks];
    if (this.filterChildId) list = list.filter(t => t.assignedTo.id === this.filterChildId);
    if (this.filterStatus) list = list.filter(t => t.status === this.filterStatus);
    return list.sort((a, b) => (STATUS_ORDER[a.status] ?? 4) - (STATUS_ORDER[b.status] ?? 4));
  }

  onCardClick(task: Task): void {
    if (task.status === 'DONE') {
      this.router.navigate(['/parent/tasks', task.id, 'review']);
    } else {
      this.detailTask = task;
      this.showDetailModal = true;
    }
  }

  openCreateModal(): void {
    this.createForm = this.emptyForm();
    this.createError = '';
    this.showCreateModal = true;
  }

  createTask(): void {
    const f = this.createForm;
    if (!f.title.trim() || !f.description.trim() || !f.category || !f.dueDateNote.trim()) {
      this.createError = 'Please fill in all required fields.';
      return;
    }
    if (!f.assignToAll && !f.assignedToId) {
      this.createError = 'Please select a child.';
      return;
    }
    if (f.tokenValue < 1) {
      this.createError = 'Token value must be at least 1.';
      return;
    }

    this.creating = true;
    this.createError = '';

    this.taskService.createTask({
      title: f.title.trim(),
      description: f.description.trim(),
      category: f.category as TaskCategory,
      tokenValue: f.tokenValue,
      dueDateNote: f.dueDateNote.trim(),
      assignToAll: f.assignToAll,
      assignedToId: f.assignToAll ? undefined : f.assignedToId || undefined
    }).subscribe({
      next: () => {
        this.showCreateModal = false;
        this.creating = false;
        this.load();
      },
      error: err => {
        this.createError = err.error?.error || 'Failed to create task.';
        this.creating = false;
      }
    });
  }

  statusLabel(status: string): string {
    const map: Record<string, string> = {
      OPEN: 'Open', IN_PROGRESS: 'In Progress', DONE: 'Submitted', REVIEWED: 'Reviewed'
    };
    return map[status] ?? status;
  }

  statusBadgeClass(status: string): string {
    const map: Record<string, string> = {
      OPEN: 'fq-badge-open', IN_PROGRESS: 'fq-badge-in-progress',
      DONE: 'fq-badge-done', REVIEWED: 'fq-badge-reviewed'
    };
    return map[status] ?? 'fq-badge-open';
  }

  categoryBadgeClass(category: TaskCategory): string {
    const map: Record<TaskCategory, string> = {
      SPORT_AND_ACTIVITY: 'fq-category-sport',
      STUDY_AND_LEARNING: 'fq-category-study',
      CHORES: 'fq-category-chores',
      HEALTH_AND_HYGIENE: 'fq-category-health',
      CREATIVE: 'fq-category-creative',
      SOCIAL_AND_FAMILY: 'fq-category-social',
      DIGITAL_WELLBEING: 'fq-category-digital'
    };
    return map[category] ?? 'fq-category-study';
  }

  private emptyForm() {
    return { title: '', description: '', category: '' as TaskCategory | '', tokenValue: 10, dueDateNote: '', assignToAll: true, assignedToId: '' };
  }
}
