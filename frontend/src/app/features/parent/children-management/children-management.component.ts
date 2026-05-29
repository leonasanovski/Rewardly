import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChildService } from '../../../core/services/child.service';
import { Child } from '../../../core/models/child.model';
import { ParentNavComponent } from '../../../shared/components/parent-nav/parent-nav.component';

@Component({
  selector: 'app-children-management',
  standalone: true,
  imports: [CommonModule, FormsModule, ParentNavComponent],
  templateUrl: './children-management.component.html',
  styleUrl: './children-management.component.scss'
})
export class ChildrenManagementComponent implements OnInit {
  children: Child[] = [];
  loading = true;
  error = '';

  showForm = false;
  editingId: string | null = null;
  formData = { fullName: '', username: '', password: '' };
  formError = '';
  saving = false;

  deletingId: string | null = null;
  deleting = false;
  deleteError = '';

  adjustingId: string | null = null;
  adjustAmount = 0;
  adjusting = false;
  adjustError = '';

  newChildCredentials: { id: string; username: string; password: string } | null = null;

  toastMessage = '';
  showToast = false;
  private toastTimer: ReturnType<typeof setTimeout> | null = null;

  constructor(private childService: ChildService) {}

  ngOnInit(): void {
    this.loadChildren();
  }

  private loadChildren(): void {
    this.loading = true;
    this.childService.getChildren().subscribe({
      next: children => {
        this.children = children;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load children.';
        this.loading = false;
      }
    });
  }

  openAdd(): void {
    this.editingId = null;
    this.formData = { fullName: '', username: '', password: '' };
    this.formError = '';
    this.showForm = true;
    this.adjustingId = null;
    this.deletingId = null;
  }

  openEdit(child: Child): void {
    this.editingId = child.id;
    this.formData = { fullName: child.fullName, username: child.username, password: '' };
    this.formError = '';
    this.showForm = true;
    this.deletingId = null;
    this.adjustingId = null;
  }

  cancelForm(): void {
    this.showForm = false;
    this.editingId = null;
    this.formError = '';
  }

  saveChild(): void {
    const f = this.formData;
    if (!f.fullName.trim() || !f.username.trim()) {
      this.formError = 'Full name and username are required.';
      return;
    }
    if (!this.editingId && !f.password.trim()) {
      this.formError = 'Password is required.';
      return;
    }

    this.saving = true;
    this.formError = '';

    if (this.editingId) {
      const payload: { fullName?: string; username?: string; password?: string } = {
        fullName: f.fullName.trim(),
        username: f.username.trim()
      };
      if (f.password.trim()) payload.password = f.password.trim();

      this.childService.updateChild(this.editingId, payload).subscribe({
        next: () => {
          this.saving = false;
          this.showForm = false;
          this.editingId = null;
          this.loadChildren();
        },
        error: err => {
          this.formError = err.error?.error || 'Failed to update child.';
          this.saving = false;
        }
      });
    } else {
      this.childService.createChild({
        fullName: f.fullName.trim(),
        username: f.username.trim(),
        password: f.password.trim()
      }).subscribe({
        next: child => {
          this.newChildCredentials = { id: child.id, username: child.username, password: f.password.trim() };
          this.saving = false;
          this.showForm = false;
          this.loadChildren();
        },
        error: err => {
          this.formError = err.error?.error || 'Failed to create child.';
          this.saving = false;
        }
      });
    }
  }

  startDelete(child: Child): void {
    this.deletingId = child.id;
    this.deleteError = '';
    this.showForm = false;
    this.adjustingId = null;
  }

  cancelDelete(): void {
    this.deletingId = null;
    this.deleteError = '';
  }

  confirmDelete(child: Child): void {
    this.deleting = true;
    this.childService.deleteChild(child.id).subscribe({
      next: () => {
        this.deletingId = null;
        this.deleting = false;
        if (this.newChildCredentials?.id === child.id) this.newChildCredentials = null;
        this.loadChildren();
      },
      error: err => {
        this.deleteError = err.error?.error || 'Failed to delete child.';
        this.deleting = false;
      }
    });
  }

  openAdjust(child: Child): void {
    this.adjustingId = child.id;
    this.adjustAmount = 0;
    this.adjustError = '';
    this.showForm = false;
    this.deletingId = null;
  }

  cancelAdjust(): void {
    this.adjustingId = null;
    this.adjustAmount = 0;
    this.adjustError = '';
  }

  applyAdjust(child: Child): void {
    if (this.adjustAmount === 0) {
      this.adjustError = 'Amount cannot be zero.';
      return;
    }
    this.adjusting = true;
    this.adjustError = '';

    this.childService.adjustTokens(child.id, this.adjustAmount).subscribe({
      next: updated => {
        const idx = this.children.findIndex(c => c.id === child.id);
        if (idx >= 0) this.children[idx] = updated;
        this.adjustingId = null;
        this.adjusting = false;
        const abs = Math.abs(this.adjustAmount);
        const verb = this.adjustAmount > 0 ? 'awarded' : 'deducted';
        this.toast(`${abs} token${abs !== 1 ? 's' : ''} ${verb} — ${updated.fullName} now has ${updated.tokens}.`);
      },
      error: err => {
        this.adjustError = err.error?.error || 'Failed to adjust tokens.';
        this.adjusting = false;
      }
    });
  }

  resultingBalance(child: Child): number {
    return Math.max(0, child.tokens + this.adjustAmount);
  }

  copyCredentials(child: Child): void {
    const creds = this.newChildCredentials?.id === child.id
      ? `Username: ${child.username}\nPassword: ${this.newChildCredentials.password}`
      : `Username: ${child.username}`;

    navigator.clipboard.writeText(creds).then(() => {
      this.toast('Credentials copied to clipboard!');
    }).catch(() => {
      this.toast('Could not access clipboard.');
    });
  }

  dismissNewCredentials(): void {
    this.newChildCredentials = null;
  }

  private toast(message: string): void {
    if (this.toastTimer) clearTimeout(this.toastTimer);
    this.toastMessage = message;
    this.showToast = true;
    this.toastTimer = setTimeout(() => { this.showToast = false; }, 3500);
  }
}
