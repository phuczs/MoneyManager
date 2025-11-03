# Git Version Control Guide for MoneyManager

## 📚 **Git Workflow for Development**

### **Basic Git Commands You'll Use Daily**

#### **1. Check Status**
```bash
git status                    # See what files are changed
git log --oneline            # View commit history
git log --graph --oneline    # View commit history with branch visualization
```

#### **2. Making Changes**
```bash
# After making code changes:
git add .                    # Stage all changes
git add specific-file.kt     # Stage specific file
git commit -m "Your message" # Commit with descriptive message
```

#### **3. Working with Branches**
```bash
# Create and switch to new feature branch
git checkout -b feature/transaction-filtering
git checkout -b fix/balance-calculation

# Switch between branches
git checkout master          # Switch to main branch
git checkout feature/transaction-filtering

# List all branches
git branch                   # Local branches
git branch -a               # All branches (local + remote)
```

#### **4. Merging Changes**
```bash
# Merge feature branch into master
git checkout master
git merge feature/transaction-filtering

# Delete merged branch
git branch -d feature/transaction-filtering
```

### **🔄 Recommended Development Workflow**

#### **For New Features:**
1. **Create Feature Branch**
   ```bash
   git checkout -b feature/expense-categories
   ```

2. **Make Changes** (code, test, debug)

3. **Commit Regularly**
   ```bash
   git add .
   git commit -m "Add expense category creation UI"
   ```

4. **More commits as you progress**
   ```bash
   git commit -m "Implement category validation logic"
   git commit -m "Add unit tests for category service"
   ```

5. **Merge to Main When Complete**
   ```bash
   git checkout master
   git merge feature/expense-categories
   git branch -d feature/expense-categories
   ```

#### **For Bug Fixes:**
1. **Create Fix Branch**
   ```bash
   git checkout -b fix/balance-loading-issue
   ```

2. **Fix the Bug**

3. **Commit Fix**
   ```bash
   git add .
   git commit -m "Fix: Balance now calculates from all transactions

   - Changed dashboard to load all transactions instead of recent 5
   - Fixed Firestore query to avoid index requirements
   - Updated balance calculation logic in BalanceSummaryCard"
   ```

4. **Merge Back**
   ```bash
   git checkout master
   git merge fix/balance-loading-issue
   ```

### **📝 Commit Message Best Practices**

#### **Good Commit Messages:**
```bash
git commit -m "Add transaction filtering by date range"
git commit -m "Fix: Categories not loading on first app launch"
git commit -m "Refactor: Extract balance calculation to utility class"
git commit -m "Update: Improve UI responsiveness on dashboard"
```

#### **Message Types:**
- **Add:** New features
- **Fix:** Bug fixes
- **Update:** Improvements to existing features
- **Refactor:** Code restructuring without changing functionality
- **Remove:** Deleting code/features
- **Docs:** Documentation changes

### **🚨 Important Git Rules**

#### **Never Commit These Files:**
- `local.properties` (contains SDK paths)
- `/build/` directories
- `.idea/` files (IDE settings)
- Temporary files
- API keys or secrets

#### **Always Commit These:**
- Source code (`.kt`, `.java`)
- Resource files (`.xml`, images)
- Gradle build files
- Documentation
- Configuration files

### **🔧 Advanced Git Commands**

#### **Undo Changes:**
```bash
# Undo uncommitted changes
git checkout -- filename.kt    # Undo changes to specific file
git reset --hard               # Undo ALL uncommitted changes (CAREFUL!)

# Undo last commit (but keep changes)
git reset --soft HEAD~1

# Undo last commit (and discard changes)
git reset --hard HEAD~1
```

#### **View Changes:**
```bash
git diff                       # See unstaged changes
git diff --staged             # See staged changes
git diff HEAD~1               # Compare with previous commit
```

#### **Stash Changes:**
```bash
git stash                     # Temporarily save changes
git stash pop                 # Restore stashed changes
git stash list                # See all stashes
```

### **📊 Project-Specific Workflow Examples**

#### **Adding a New Screen:**
```bash
git checkout -b feature/settings-screen
# Create SettingsScreen.kt
# Update navigation
# Add ViewModel
git add .
git commit -m "Add settings screen with user preferences"
git checkout master
git merge feature/settings-screen
```

#### **Fixing Firebase Issues:**
```bash
git checkout -b fix/firebase-auth-error
# Fix authentication logic
# Test thoroughly
git add .
git commit -m "Fix: Firebase auth error on app startup

- Added proper error handling in AuthViewModel
- Fixed null pointer exception in authentication flow
- Added retry mechanism for network failures"
git checkout master
git merge fix/firebase-auth-error
```

### **🔗 Setting Up Remote Repository (Optional)**

If you want to backup to GitHub/GitLab:

```bash
# Add remote repository
git remote add origin https://github.com/yourusername/MoneyManager.git

# Push to remote
git push -u origin master

# Future pushes
git push
```

### **📋 Daily Git Checklist**

- [ ] Check status before starting work: `git status`
- [ ] Create feature branch for new work
- [ ] Commit changes regularly with clear messages
- [ ] Test your changes before committing
- [ ] Merge completed features back to master
- [ ] Keep commit history clean and meaningful

---

**Remember:** Git is your safety net - commit early and often!