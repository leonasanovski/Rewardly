import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { RewardService } from '../../../core/services/reward.service';
import { TokenStorageService } from '../../../core/services/token-storage.service';
import { WalletResponse, RewardStatus } from '../../../core/models/reward.model';
import { ChildNavComponent } from '../../../shared/components/child-nav/child-nav.component';

@Component({
  selector: 'app-wallet',
  standalone: true,
  imports: [CommonModule, RouterLink, ChildNavComponent],
  templateUrl: './wallet.component.html',
  styleUrl: './wallet.component.scss'
})
export class WalletComponent implements OnInit {
  wallet: WalletResponse | null = null;
  loading = true;
  error = '';

  constructor(private rewardService: RewardService, private tokenStorage: TokenStorageService) {}

  ngOnInit(): void {
    this.rewardService.getWallet().subscribe({
      next: wallet => {
        this.wallet = wallet;
        this.tokenStorage.updateTokens(wallet.tokens);
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load wallet.';
        this.loading = false;
      }
    });
  }

  statusBadgeClass(status: RewardStatus): string {
    return status === 'AVAILABLE' ? 'fq-badge-available' : 'fq-badge-redeemed';
  }

  statusLabel(status: RewardStatus): string {
    return status === 'AVAILABLE' ? 'Available' : 'Redeemed';
  }
}
