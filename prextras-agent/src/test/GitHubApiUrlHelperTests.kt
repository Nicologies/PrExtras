import com.nicologies.prextras.GitHubApiUrlHelper
import com.nicologies.prextras.common.SettingsKeys
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class GitHubApiUrlHelperTests{
    @Test
    fun prependProtocolIfNotPresent(){
        val systemProperties = HashMap<String, String>()
        systemProperties.put(SettingsKeys.GitHubApiUrl, "api.github.com")
        assertThat(GitHubApiUrlHelper.getURL(systemProperties).protocol).isEqualTo("https")
    }

    @Test
    fun shouldNotPrependProtocolForUrlStartsWithHttp(){
        val systemProperties = HashMap<String, String>()
        systemProperties.put(SettingsKeys.GitHubApiUrl, "http://api.github.com")
        assertThat(GitHubApiUrlHelper.getURL(systemProperties).protocol).isEqualTo("http")
        systemProperties.clear()
        systemProperties.put(SettingsKeys.GitHubApiUrl, "https://api.github.com")
        assertThat(GitHubApiUrlHelper.getURL(systemProperties).protocol).isEqualTo("https")
    }
}