package com.rewardly.config

import com.rewardly.entity.*
import com.rewardly.repository.*
import jakarta.annotation.PostConstruct
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class DataSeeder(
    val userRepository: UserRepository,
    val taskRepository: TaskRepository,
    val rewardRepository: RewardRepository,
    val purchasedRewardRepository: PurchasedRewardRepository,
    val passwordEncoder: PasswordEncoder
) {

    @PostConstruct
    fun seed() {
        if (userRepository.count() > 0) return
        val (marija, filip, sara) = seedUsers()
        seedTasks(marija, filip, sara)
        val rewards = seedRewards(marija, filip, sara)
        seedPurchasedRewards(filip, sara, rewards)
    }

    private fun seedUsers(): Triple<User, User, User> {
        val marija = userRepository.save(User(
            fullName = "Marija Stojanovic",
            email = "marija@test.com",
            passwordHash = passwordEncoder.encode("test123"),
            role = Role.PARENT,
            embg = "1505975123456"
        ))
        val filip = userRepository.save(User(
            fullName = "Filip Stojanovic",
            username = "filip",
            passwordHash = passwordEncoder.encode("test123"),
            role = Role.CHILD,
            tokens = 120,
            parent = marija
        ))
        val sara = userRepository.save(User(
            fullName = "Sara Stojanovic",
            username = "sara",
            passwordHash = passwordEncoder.encode("test123"),
            role = Role.CHILD,
            tokens = 45,
            parent = marija
        ))
        return Triple(marija, filip, sara)
    }

    private fun seedTasks(marija: User, filip: User, sara: User) {
        // Task 1 — REVIEWED (Filip)
        taskRepository.save(Task(
            title = "Go for a 30-minute walk outside",
            description = "Walk around the neighborhood or the park. Bring water with you. When you get back, write a few sentences about how you felt during the walk.",
            category = TaskCategory.SPORT_AND_ACTIVITY,
            tokenValue = 100,
            dueDateNote = "Try to finish this before Sunday because we have guests coming and I want you to be active before then.",
            status = TaskStatus.REVIEWED,
            assignedTo = filip,
            createdBy = marija,
            startedAt = LocalDateTime.now().minusDays(5).withHour(14).withMinute(10),
            finishedAt = LocalDateTime.now().minusDays(5).withHour(15).withMinute(45),
            childNote = "I walked for 35 minutes around the park. It was a bit cold but I felt really good after. I also took some photos along the way.",
            childSelfRating = 4,
            parentRating = 4,
            parentComment = "Great job Filip! Next time try to walk a bit longer. I am proud of you.",
            tokensEarned = 80
        ))

        // Task 2 — REVIEWED (Filip)
        taskRepository.save(Task(
            title = "Read a book for 20 minutes",
            description = "Choose any book you like and read for at least 20 minutes. Write down one thing you learned or one thing you liked about what you read.",
            category = TaskCategory.STUDY_AND_LEARNING,
            tokenValue = 60,
            dueDateNote = "Finish this during the week, any day works. Reading before bed is a great habit!",
            status = TaskStatus.REVIEWED,
            assignedTo = filip,
            createdBy = marija,
            startedAt = LocalDateTime.now().minusDays(3).withHour(20).withMinute(0),
            finishedAt = LocalDateTime.now().minusDays(3).withHour(20).withMinute(25),
            childNote = "I read Harry Potter for 22 minutes. I liked the part where Harry finds out he is a wizard. It was really exciting.",
            childSelfRating = 5,
            parentRating = 5,
            parentComment = "Perfect! I love that you enjoyed it. Keep reading every night!",
            tokensEarned = 60
        ))

        // Task 3 — DONE (Filip) — waiting for parent review
        taskRepository.save(Task(
            title = "Clean your room",
            description = "Vacuum the floor, put all clothes in the wardrobe, and organize your desk. Take a photo when you are done so I can see.",
            category = TaskCategory.CHORES,
            tokenValue = 80,
            dueDateNote = "Please finish this by Friday evening. We have relatives visiting on Saturday and I want the house to be tidy.",
            status = TaskStatus.DONE,
            assignedTo = filip,
            createdBy = marija,
            startedAt = LocalDateTime.now().minusDays(1).withHour(11).withMinute(30),
            finishedAt = LocalDateTime.now().minusDays(1).withHour(12).withMinute(15),
            childNote = "I cleaned everything! Vacuumed the floor, put all my clothes away and organized my desk. I also took out the trash from my room.",
            childSelfRating = 4
        ))

        // Task 4 — IN_PROGRESS (Filip)
        taskRepository.save(Task(
            title = "Drink 8 glasses of water today",
            description = "Track how much water you drink today. Every time you finish a glass, write it down. Try to reach 8 glasses before bedtime.",
            category = TaskCategory.HEALTH_AND_HYGIENE,
            tokenValue = 40,
            dueDateNote = "This is a daily habit task. Try to do it today and see how it feels. Staying hydrated helps you focus better at school!",
            status = TaskStatus.IN_PROGRESS,
            assignedTo = filip,
            createdBy = marija,
            startedAt = LocalDateTime.now().withHour(9).withMinute(0)
        ))

        // Task 5 — OPEN (Filip)
        taskRepository.save(Task(
            title = "Help set the table for dinner",
            description = "Set the table for the whole family before dinner. Put out plates, glasses, forks, knives and napkins. Ask me what time dinner will be ready.",
            category = TaskCategory.SOCIAL_AND_FAMILY,
            tokenValue = 30,
            dueDateNote = "Do this today before 7pm when dinner will be ready. It is a small thing but it means a lot to the family.",
            assignedTo = filip,
            createdBy = marija
        ))

        // Task 6 — OPEN (Filip)
        taskRepository.save(Task(
            title = "Spend 1 hour without any screen",
            description = "For one full hour, put away your phone, tablet, and turn off the TV. Use that time to draw, read, go outside, or help around the house. Write what you did with that hour.",
            category = TaskCategory.DIGITAL_WELLBEING,
            tokenValue = 50,
            dueDateNote = "Try this during the afternoon today. You spend a lot of time on screens and I want you to see how it feels to take a break. You might enjoy it!",
            assignedTo = filip,
            createdBy = marija
        ))

        // Task 7 — REVIEWED (Sara)
        taskRepository.save(Task(
            title = "Draw something creative",
            description = "Draw anything you want — a landscape, an animal, a character, or something from your imagination. Use colors and take your time. I would love to see what you create!",
            category = TaskCategory.CREATIVE,
            tokenValue = 70,
            dueDateNote = "No rush on this one, finish it whenever you feel inspired this week. Creativity takes time!",
            status = TaskStatus.REVIEWED,
            assignedTo = sara,
            createdBy = marija,
            startedAt = LocalDateTime.now().minusDays(4).withHour(16).withMinute(0),
            finishedAt = LocalDateTime.now().minusDays(4).withHour(17).withMinute(30),
            childNote = "I drew a sunset over the mountains with a lake in front. I used watercolor pencils. It took me about an hour and a half.",
            childSelfRating = 5,
            parentRating = 5,
            parentComment = "Sara this is absolutely beautiful! You are so talented. I am going to hang this on the wall.",
            tokensEarned = 70
        ))

        // Task 8 — DONE (Sara) — waiting for parent review
        taskRepository.save(Task(
            title = "Do 20 minutes of homework",
            description = "Sit at your desk without distractions and work on your school homework for at least 20 minutes. Write which subject you worked on and what you did.",
            category = TaskCategory.STUDY_AND_LEARNING,
            tokenValue = 50,
            dueDateNote = "Please do this today after school, before 6pm. Doing homework on time makes the evenings much more relaxed for everyone.",
            status = TaskStatus.DONE,
            assignedTo = sara,
            createdBy = marija,
            startedAt = LocalDateTime.now().withHour(15).withMinute(30),
            finishedAt = LocalDateTime.now().withHour(16).withMinute(5),
            childNote = "I did my math homework for 35 minutes. We had exercises with fractions and I finished all of them. I also started reading for my literature class.",
            childSelfRating = 4
        ))

        // Task 9 — OPEN (Sara)
        taskRepository.save(Task(
            title = "Go for a bike ride",
            description = "Take your bike out and ride for at least 20 minutes. You can go around the neighborhood or to the park. Wear your helmet!",
            category = TaskCategory.SPORT_AND_ACTIVITY,
            tokenValue = 90,
            dueDateNote = "The weather is nice this week, try to go out before Thursday. Physical activity is really important for your health and mood.",
            assignedTo = sara,
            createdBy = marija
        ))
    }

    private fun seedRewards(marija: User, filip: User, sara: User): List<Reward> {
        val r1 = rewardRepository.save(Reward(
            title = "2 hours extra gaming",
            description = "You can play video games or use your tablet for 2 extra hours on any day you choose. Just let me know in advance!",
            tokenCost = 80,
            assignToAll = true,
            createdBy = marija
        ))
        val r2 = rewardRepository.save(Reward(
            title = "Choose dinner tonight",
            description = "You get to choose what we have for dinner tonight. I will cook it or we can order it together. Any meal you want!",
            tokenCost = 40,
            assignToAll = true,
            createdBy = marija
        ))
        rewardRepository.save(Reward(
            title = "Stay up 1 hour later",
            description = "You can stay up one hour past your normal bedtime tonight. You can watch a movie, read, or just relax.",
            tokenCost = 60,
            assignToAll = true,
            createdBy = marija
        ))
        rewardRepository.save(Reward(
            title = "New football",
            description = "I will buy you a new football of your choice. We can go to the sports shop together and you can pick the one you like.",
            tokenCost = 300,
            assignToAll = false,
            assignedTo = filip,
            createdBy = marija
        ))
        rewardRepository.save(Reward(
            title = "New drawing set",
            description = "I will buy you a new professional drawing set with watercolors, markers, and colored pencils. You deserve it for all your creative work!",
            tokenCost = 250,
            assignToAll = false,
            assignedTo = sara,
            createdBy = marija
        ))
        val r6 = rewardRepository.save(Reward(
            title = "Movie night — you pick the film",
            description = "We will have a family movie night and you get to choose the film. I will make popcorn. Pick any movie you want to watch together.",
            tokenCost = 35,
            assignToAll = true,
            createdBy = marija
        ))
        return listOf(r1, r2, r6)
    }

    private fun seedPurchasedRewards(filip: User, sara: User, rewards: List<Reward>) {
        val (r1, r2, r6) = rewards

        // Filip — "2 hours extra gaming" (REDEEMED)
        purchasedRewardRepository.save(PurchasedReward(
            reward = r1,
            child = filip,
            status = RewardStatus.REDEEMED,
            purchasedAt = LocalDateTime.now().minusDays(6).withHour(18).withMinute(0),
            redeemedAt = LocalDateTime.now().minusDays(5).withHour(20).withMinute(0)
        ))

        // Filip — "Choose dinner tonight" (AVAILABLE)
        purchasedRewardRepository.save(PurchasedReward(
            reward = r2,
            child = filip,
            status = RewardStatus.AVAILABLE,
            purchasedAt = LocalDateTime.now().minusDays(1).withHour(17).withMinute(30)
        ))

        // Sara — "Movie night" (AVAILABLE)
        purchasedRewardRepository.save(PurchasedReward(
            reward = r6,
            child = sara,
            status = RewardStatus.AVAILABLE,
            purchasedAt = LocalDateTime.now().withHour(10).withMinute(0)
        ))
    }
}
