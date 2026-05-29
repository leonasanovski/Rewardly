export type RewardStatus = 'AVAILABLE' | 'REDEEMED';

export interface Reward {
  id: string;
  title: string;
  description: string;
  tokenCost: number;
  assignToAll: boolean;
  assignedTo?: { id: string; fullName: string; username: string };
}

export interface CreateRewardRequest {
  title: string;
  description: string;
  tokenCost: number;
  assignToAll: boolean;
  assignedToId?: string;
}

export interface PurchasedReward {
  id: string;
  reward: { id: string; title: string; description: string; tokenCost: number };
  status: RewardStatus;
  purchasedAt: string;
  redeemedAt?: string;
}

export interface WalletResponse {
  tokens: number;
  purchasedRewards: PurchasedReward[];
}
