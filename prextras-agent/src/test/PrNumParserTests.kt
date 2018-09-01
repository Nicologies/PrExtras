import com.nicologies.prextras.PrNumParser
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class PrNumParserTests{
    private val expectedPrNum = "1234"

    @Test
    fun ableToParsePrNumber(){
        doTestParsePrNum(teamcityBuildBranch="$expectedPrNum", expectedPrNum= expectedPrNum, expectedToBeAPr = true)
        doTestParsePrNum(teamcityBuildBranch="refs/pull/$expectedPrNum/head", expectedPrNum= expectedPrNum, expectedToBeAPr = true)
        doTestParsePrNum(teamcityBuildBranch="refs/pull/$expectedPrNum/merge", expectedPrNum= expectedPrNum, expectedToBeAPr = true)
        doTestParsePrNum(teamcityBuildBranch="$expectedPrNum/head", expectedPrNum= expectedPrNum, expectedToBeAPr = true)
        doTestParsePrNum(teamcityBuildBranch="$expectedPrNum/merge", expectedPrNum= expectedPrNum, expectedToBeAPr = true)
        doTestParsePrNum(teamcityBuildBranch="pull/$expectedPrNum/merge", expectedPrNum= expectedPrNum, expectedToBeAPr = true)
        doTestParsePrNum(teamcityBuildBranch="pull/$expectedPrNum", expectedPrNum= expectedPrNum, expectedToBeAPr = true)
        doTestParsePrNum(teamcityBuildBranch="refs/pull/$expectedPrNum", expectedPrNum= expectedPrNum, expectedToBeAPr = true)
    }

    @Test
    fun notPr(){
        doTestParsePrNum(teamcityBuildBranch="refs$expectedPrNum/merge", expectedPrNum= "refs$expectedPrNum/merge", expectedToBeAPr = false)
        doTestParsePrNum(teamcityBuildBranch="refs$expectedPrNum", expectedPrNum= "refs$expectedPrNum", expectedToBeAPr = false)
    }

    private fun doTestParsePrNum(teamcityBuildBranch: String, expectedPrNum: String, expectedToBeAPr: Boolean) {
        val (pr, isPr) = PrNumParser.parse(teamcityBuildBranch)
        assertThat(isPr).`as`("check if $teamcityBuildBranch is a pr").isEqualTo(expectedToBeAPr)
        assertThat(pr).`as`("check $teamcityBuildBranch's pr num").isEqualTo(expectedPrNum)
    }
}