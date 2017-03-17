import com.nicologies.prextras.RepoInfoParser
import org.junit.Test
import org.assertj.core.api.Assertions.*
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class RepoInfoParserTests(val repoUrl: String){
   companion object {
        val owner = "owner"
        val repo = "repo"

        @JvmStatic
        @Parameterized.Parameters
        fun Urls(): Collection<Array<String>> {
            return listOf(
                    arrayOf("http://github.com/$owner/$repo.git"),
                    arrayOf("https://github.com/$owner/$repo.git"),
                    arrayOf("https://github.com/$owner/$repo"),
                    arrayOf("git@github.com:$owner/$repo.git"),
                    arrayOf("git@ebenzgithub-ghe.australiasoutheast.cloudapp.azure.com:$owner/$repo.git"),
                    arrayOf("ssh://user@host.xz:$owner/$repo.git"),
                    arrayOf("ssh://host.xz:$owner/$repo.git"),
                    arrayOf("ssh://git@myserver.github.com/$owner/$repo.git"),
                    arrayOf("https://git@github.com/$owner/$repo")
            )
        }
    }

    @Test
    fun canParseValidGitUrls(){
        var repoId = RepoInfoParser.Parse(repoUrl)
        assertThat(repoId.owner).isEqualTo(owner)
        assertThat(repoId.name).isEqualTo(repo)
    }
}