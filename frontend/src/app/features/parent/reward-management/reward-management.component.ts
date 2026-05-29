import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { RewardService } from '../../../core/services/reward.service';
import { ChildService } from '../../../core/services/child.service';
import { Reward, WalletResponse, PurchasedReward } from '../../../core/models/reward.model';
import { Child } from '../../../core/models/child.model';
import { ParentNavComponent } from '../../../shared/components/parent-nav/parent-nav.component';

@Component({
  selector: 'app-reward-management',
  standalone: true,
  imports: [CommonModule, FormsModule, ParentNavComponent],
  templateUrl: './reward-management.component.html',
  styleUrl: './reward-management.component.scss'
})
export class RewardManagementComponent implements OnInit {
  rewards: Reward[] = [];
  children: Child[] = [];
  childWallets: Record<string, WalletResponse> = {};
  loading = true;
  error = '';

  activeSection: 'catalogue' | 'wallets' = 'catalogue';
  expandedChildren: Record<string, boolean> = {};

  showForm = false;
  editingId: string | null = null;
  formData = { title: '', description: '', tokenCost: 10, assignToAll: true, assignedToId: '' };
  formError = '';
  saving = false;

  deletingId: string | null = null;
  deleting = false;
  deleteError = '';

  toastMessage = '';
  showToast = false;
  private toastTimer: ReturnType<typeof setTimeout> | null = null;

  constructor(
    private rewardService: RewardService,
    private childService: ChildService
  ) {}

  ngOnInit(): void {
    this.load();
  }

  private load(): void {
    this.loading = true;
    forkJoin({
      rewards: this.rewardService.getRewards(),
      children: this.childService.getChildren()
    }).subscribe({
      next: ({ rewards, children }) => {
        this.rewards = rewards;
        this.children = children;
        this.loading = false;
        this.loadAllWallets();
      },
      error: () => {
        this.error = 'Failed to load data.';
        this.loading = false;
      }
    });
  }

  private loadAllWallets(): void {
    if (this.children.length === 0) return;
    forkJoin(this.children.map(c => this.rewardService.getChildWallet(c.id))).subscribe({
      next: wallets => {
        const updated: Record<string, WalletResponse> = {};
        wallets.forEach((w, i) => { updated[this.children[i].id] = w; });
        this.childWallets = updated;
      }
    });
  }

  toggleChild(childId: string): void {
    this.expandedChildren = { ...this.expandedChildren, [childId]: !this.expandedChildren[childId] };
  }

  redeemPurchase(childId: string, purchase: PurchasedReward): void {
    const childName = this.children.find(c => c.id === childId)?.fullName ?? 'Child';
    this.rewardService.redeemPurchased(purchase.id).subscribe({
      next: updated => {
        const wallet = this.childWallets[childId];
        if (wallet) {
          const purchases = wallet.purchasedRewards.map(p => p.id === purchase.id ? updated : p);
          this.childWallets = { ...this.childWallets, [childId]: { ...wallet, purchasedRewards: purchases } };
        }
        this.toast(`${childName}'s reward marked as redeemed.`);
      },
      error: () => this.toast('Failed to redeem reward.')
    });
  }

  openAdd(): void {
    this.editingId = null;
    this.formData = { title: '', description: '', tokenCost: 10, assignToAll: true, assignedToId: '' };
    this.formError = '';
    this.showForm = true;
    this.deletingId = null;
  }

  openEdit(reward: Reward): void {
    this.editingId = reward.id;
    this.formData = {
      title: reward.title,
      description: reward.description,
      tokenCost: reward.tokenCost,
      assignToAll: reward.assignToAll,
      assignedToId: reward.assignedTo?.id ?? ''
    };
    this.formError = '';
    this.showForm = true;
    this.deletingId = null;
  }

  cancelForm(): void {
    this.showForm = false;
    this.editingId = null;
    this.formError = '';
  }

  saveReward(): void {
    const f = this.formData;
    if (!f.title.trim() || !f.description.trim()) {
      this.formError = 'Title and description are required.';
      return;
    }
    if (f.tokenCost < 1) {
      this.formError = 'Token cost must be at least 1.';
      return;
    }
    if (!f.assignToAll && !f.assignedToId) {
      this.formError = 'Please select a child.';
      return;
    }

    this.saving = true;
    this.formError = '';

    const payload = {
      title: f.title.trim(),
      description: f.description.trim(),
      tokenCost: f.tokenCost,
      assignToAll: f.assignToAll,
      assignedToId: f.assignToAll ? undefined : f.assignedToId || undefined
    };

    const op$ = this.editingId
      ? this.rewardService.updateReward(this.editingId, payload)
      : this.rewardService.createReward(payload);

    op$.subscribe({
      next: () => {
        this.saving = false;
        this.showForm = false;
        this.editingId = null;
        this.load();
      },
      error: err => {
        this.formError = err.error?.error || 'Failed to save reward.';
        this.saving = false;
      }
    });
  }

  startDelete(reward: Reward): void {
    this.deletingId = reward.id;
    this.deleteError = '';
    this.showForm = false;
  }

  cancelDelete(): void {
    this.deletingId = null;
    this.deleteError = '';
  }

  confirmDelete(reward: Reward): void {
    this.deleting = true;
    this.rewardService.deleteReward(reward.id).subscribe({
      next: () => {
        this.deletingId = null;
        this.deleting = false;
        this.load();
      },
      error: err => {
        this.deleteError = err.error?.error || 'Failed to delete reward.';
        this.deleting = false;
      }
    });
  }

  private toast(message: string): void {
    if (this.toastTimer) clearTimeout(this.toastTimer);
    this.toastMessage = message;
    this.showToast = true;
    this.toastTimer = setTimeout(() => { this.showToast = false; }, 3500);
  }
}
