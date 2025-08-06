const fs = require('fs')

const url = "https://api.github.com/repos/DataFlowAnalysis/DataFlowAnalysis/releases/latest"

async function main() {
    const r = await fetch(url);
    if (!r.ok) {
        return
    } 
    const data = await r.json();
    const version = data.tag_name;
    const date = new Date(data.published_at)
    const result = JSON.stringify({
        version: version,
        date: date.getTime(),
    });
    fs.writeFile('download/version.json', result, () => {})
}

void main()