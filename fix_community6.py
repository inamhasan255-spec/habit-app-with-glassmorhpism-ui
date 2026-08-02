with open("app/src/main/java/com/example/ui/screens/CommunityScreen.kt", "r") as f:
    text = f.read()

# Fix Unresolved reference 'Groups' in MainActivity again...
with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    main = f.read()

if "import androidx.compose.material.icons.filled.Groups" not in main:
    main = main.replace("import androidx.compose.material.icons.filled.Warning", "import androidx.compose.material.icons.filled.Warning\nimport androidx.compose.material.icons.filled.Groups")
    with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
        f.write(main)

# Re-expose the viewModel repository. It didn't seem to apply correctly earlier or something else is wrong.
with open("app/src/main/java/com/example/data/FocusViewModel.kt", "r") as f:
    vm = f.read()

# Ensure we're replacing the exact declaration.
import re
vm = re.sub(r'private val repository:\s*FocusRepository', 'val repository: FocusRepository', vm)

# Let's also check if community groups exist on the repo, perhaps they didn't get added earlier.
with open("app/src/main/java/com/example/data/FocusRepository.kt", "r") as f:
    repo = f.read()
    
if "allCommunityGroups" not in repo:
    repo = repo.replace("}", """
    // Community 
    val allCommunityGroups: kotlinx.coroutines.flow.Flow<List<CommunityGroup>> = communityGroupDao.getAllGroups()
    
    suspend fun insertCommunityGroup(group: CommunityGroup): Long = communityGroupDao.insertGroup(group)
    suspend fun deleteCommunityGroup(group: CommunityGroup) = communityGroupDao.deleteGroup(group)

    fun getMembersForGroup(groupId: Int): kotlinx.coroutines.flow.Flow<List<CommunityMember>> = communityMemberDao.getMembersForGroup(groupId)
    
    suspend fun insertCommunityMember(member: CommunityMember) = communityMemberDao.insertMember(member)
    suspend fun updateCommunityMember(member: CommunityMember) = communityMemberDao.updateMember(member)
    suspend fun deleteCommunityMember(member: CommunityMember) = communityMemberDao.deleteMember(member)
}""")
    with open("app/src/main/java/com/example/data/FocusRepository.kt", "w") as f:
        f.write(repo)
        
with open("app/src/main/java/com/example/data/FocusViewModel.kt", "w") as f:
    f.write(vm)

