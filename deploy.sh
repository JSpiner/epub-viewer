export TRAVIS_REPO_SLUG='JSpiner/epub-viewer'
export TRAVIS_PULL_REQUEST=35

export REPORT_PATH="./app/build/reports/jacoco/jacocoTestDebugUnitTestReport/jacocoTestDebugUnitTestReport.xml"

reportContent=$(cat $REPORT_PATH)

missedCountList=$(echo $reportContent | grep -oP '(?<=missed=\")([^\"]*)')
coveredCountList=$(echo $reportContent | grep -oP '(?<=covered=\")([^\"]*)')
missedCountSum=0
coveredCountSum=0

for i in ${!missedCountList[*]}
do
    missedCountSum=$(($missedCountSum + ${missedCountList[$i]}))
done

for i in ${!coveredCountList[*]}
do
    coveredCountSum=$((coveredCountSum + ${coveredCountList[$i]}))
done
echo $coveredCountSum
echo $missedCountSum

coverage=$(echo print\($coveredCountSum / ($missedCountSum + $coveredCountSum) * 100\) | python)
echo $coverage
echo "done"

curl -H "Authorization: token ${GITHUB_TOKEN}" -X POST \
    -d "{\"body\": \"\
    ## Pull Request Test Coverage Report for [PR ${TRAVIS_PULL_REQUEST}](https://github.com/{TRAVIS_REPO_SLUG}/{TRAVIS_PULL_REQUEST})\n \
    |  Totals | ![status](https://img.shields.io/badge/Coverage-${coverage}%20-brightgreen.svg) | \
    | :-- | --: | \
    | Covered Lines: | ${coveredCountSum} | \
    | Missed Lines: | ${missedCountSum} |" \
    "https://api.github.com/repos/${TRAVIS_REPO_SLUG}/issues/${TRAVIS_PULL_REQUEST}/comments"