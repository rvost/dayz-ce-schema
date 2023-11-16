const cp = require("child_process");

function isWin() {
    return /^win/.test(process.platform);
}

function mvnw() {
    return isWin() ? "mvnw.cmd" : "mvnw";
}

cp.execSync(mvnw() + " verify -DskipTests -Pgenerate-vscode-jars", {stdio: [0, 1, 2]});
