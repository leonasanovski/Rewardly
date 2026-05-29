import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { forkJoin } from 'rxjs';
import { RewardService } from '../../../core/services/reward.service';
import { TokenStorageService } from '../../../core/services/token-storage.service';
import { Reward } from '../../../core/models/reward.model';
import { ChildNavComponent } from '../../../shared/components/child-nav/child-nav.component';

@Component({
  selector: 'app-reward-market',
  standalone: true,
  imports: [CommonModule, ChildNavComponent],
  templateUrl: './reward-market.component.html',
  styleUrl: './reward-market.component.scss'
})
export class RewardMarketComponent implements OnInit {
  rewards: Reward[] = [];
  tokens = 0;
  loading = true;
  error = '';

  confirmingId: string | null = null;
  buying = false;
  buyErrors: Record<string, string> = {};

  toastMessage = '';
  showToast = false;
  private toastTimer: ReturnType<typeof setTimeout> | null = null;

  constructor(private rewardService: RewardService, private tokenStorage: TokenStorageService) {}

  ngOnInit(): void {
    forkJoin({
      rewards: this.rewardService.getRewards(),
      wallet: this.rewardService.getWallet()
    }).subscribe({
      next: ({ rewards, wallet }) => {
        this.rewards = rewards;
        this.tokens = wallet.tokens;
        this.tokenStorage.updateTokens(wallet.tokens);
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load rewards.';
        this.loading = false;
      }
    });
  }

  canAfford(reward: Reward): boolean {
    return this.tokens >= reward.tokenCost;
  }

  shortfall(reward: Reward): number {
    return reward.tokenCost - this.tokens;
  }

  startBuy(reward: Reward): void {
    delete this.buyErrors[reward.id];
    this.confirmingId = reward.id;
  }

  cancelBuy(): void {
    this.confirmingId = null;
  }

  confirmBuy(reward: Reward): void {
    this.buying = true;
    this.rewardService.buyReward(reward.id).subscribe({
      next: () => {
        this.tokens -= reward.tokenCost;
        this.tokenStorage.updateTokens(this.tokens);
        this.confirmingId = null;
        this.buying = false;
        this.toast('Reward purchased! Check your wallet.');
      },
      error: err => {
        this.buyErrors[reward.id] = err.error?.error || 'Purchase failed.';
        this.confirmingId = null;
        this.buying = false;
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
