import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Reward, CreateRewardRequest, PurchasedReward, WalletResponse } from '../models/reward.model';

@Injectable({ providedIn: 'root' })
export class RewardService {
  private readonly base = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getRewards(): Observable<Reward[]> {
    return this.http.get<Reward[]>(`${this.base}/rewards`);
  }

  createReward(data: CreateRewardRequest): Observable<Reward> {
    return this.http.post<Reward>(`${this.base}/rewards`, data);
  }

  updateReward(id: string, data: Partial<CreateRewardRequest>): Observable<Reward> {
    return this.http.put<Reward>(`${this.base}/rewards/${id}`, data);
  }

  deleteReward(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/rewards/${id}`);
  }

  buyReward(id: string): Observable<PurchasedReward> {
    return this.http.post<PurchasedReward>(`${this.base}/rewards/${id}/buy`, {});
  }

  getWallet(): Observable<WalletResponse> {
    return this.http.get<WalletResponse>(`${this.base}/wallet`);
  }

  getChildWallet(childId: string): Observable<WalletResponse> {
    return this.http.get<WalletResponse>(`${this.base}/wallet/${childId}`);
  }

  redeemPurchased(purchasedId: string): Observable<PurchasedReward> {
    return this.http.put<PurchasedReward>(`${this.base}/purchased/${purchasedId}/redeem`, {});
  }
}
