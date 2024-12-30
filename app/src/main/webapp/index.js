
const onRInputChange = (event, ui) => {
    changeBoardR(ui.value.toFixed(0));
}

class FormValidator {
    formXInput;
    formYInput;
    formRInput;
    submitButton;

    cachedHit;
    cachedServerTime;

    form_id;

    checkXInput = () => {
        if (this.formXInput.value === "") {
            alert("Выберите значение X.");
            return false;
        }
        if (this.formXInput.value > 3 || this.formXInput.value < -5) {
            alert("Число X должно быть в пределе [-5, 3].");
            return false;
        }

        return true;
    };

    checkYInput = () => {
        if (this.formYInput.value === "") {
            alert("Поле Y не должно быть пустым.");
            return false;
        }

        let pattern = new RegExp(/^-?\d+([\.,]\d+)?$/, "g");

        if (!pattern.test(this.formYInput.value)) {
            alert("Введите число.");
            return false;
        }

        if (this.formYInput.value > 3 || this.formYInput.value < -5) {
            alert("Число Y должно быть в пределе [-5, 3].");
            return false;
        }

        return true;
    };

    checkRInput = () => {
        if (this.formRInput.value === "") {
            alert("Выберите значение.");
            return false;
        }

        if (!["1", "2", "3", "4", "5"].includes(this.formRInput.value)) {
            this.formRInput.value = "";
            alert("Выберите НОРМАЛЬНОЕ значение.");
            return false;
        }

        return true;
    };

    constructor(form_id) {
        this.formXInput = document.getElementById(`${form_id}:form-x-input`);
        this.formYInput = document.getElementById(`${form_id}:form-y-input`);
        this.formRInput = document.getElementById(`${form_id}:form-r-input`);
        this.cachedHit = document.getElementById(`${form_id}:cached-hit`)
        this.cachedServerTime = document.getElementById(`${form_id}:cached-server-time`)
        this.submitButton = document.getElementById(`${form_id}:submit-button`);

        this.form_id = form_id;

        this.formRInput.addEventListener("change", onRInputChange);
    }

    checkAllValid = () => {
        // let isNotValid = [
        //     !this.checkRInput(),
        //     !this.checkYInput(),
        //     !this.checkXInput(),
        // ].reduce((prev, curr) => prev || curr);

        // return !isNotValid;

        return this.checkRInput() && this.checkYInput() && this.checkXInput();
    }

    addPointFromInput = (data) => {
        if (data.status !== 'success' || !this.checkAllValid()) {
            return;
        }

        let x = this.formXInput.value;
        let y = this.formYInput.value;
        let r = this.formRInput.value;
        let hit = document.getElementById(`${this.form_id}:cached-hit`).innerText == "true";
        let serverTime = document.getElementById(`${this.form_id}:cached-server-time`).innerText;
        let durationMilliseconds = document.getElementById(`${this.form_id}:cached-duration-milli-seconds`).innerText;

        // console.log({x, y, r, serverTime, hit})

        addPoint({x, y, r, serverTime, hit})
        addPointToTable({x, y, r, serverTime, hit, durationMilliseconds})
    }
}

const mainFormValidator = new FormValidator("main-form");
const hiddenFormValidator = new FormValidator("hidden-form");

async function loadHitHistory() {
    changeBoardR((+mainFormValidator.formRInput.value).toFixed(0));
    cachedPoints.forEach(element => {
        addPoint(element);
    });
}

loadHitHistory();

const onBoardClick = (x,y) => {
    hiddenFormValidator.formXInput.value = x.toFixed(0);
    hiddenFormValidator.formYInput.value = y.toFixed(2);
    hiddenFormValidator.formRInput.value = mainFormValidator.formRInput.value;

    hiddenFormValidator.submitButton.dispatchEvent(new Event('click'));
}

function addPoint({x,y,r,hit,serverTime}) {
    createNewHitPoint(x, y, r, { name: serverTime, color: hit ? "green" : "red", });
}

const table_element = document.getElementById("table");


const addPointToTable = ({x,y,r,hit,serverTime, durationMilliseconds}) => {
    let tableEntry = "<tr>";
    let data = [x, y, r, hit ? "Yes" : "No", durationMilliseconds, serverTime];
    data.forEach((str) => {
        tableEntry += `<td>${str}</td>`
    });
    tableEntry += "</tr>";

    table_element.innerHTML += tableEntry;
}
