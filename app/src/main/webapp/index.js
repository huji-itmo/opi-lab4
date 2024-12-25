
const API_ENDPOINT = "/controller"

const onSubmit = (event) => {
    if (!formValidator.checkAllValid()) {
        return;
    }

    changeBoardR(formValidator.formRInput.value)
}

const onRInputChange = (event, ui) => {
    changeBoardR(ui.value.toFixed(0));
}

const onXInputChange = (event, ui) => {
}

const onYInputChange = (event, ui) => {
}

class FormValidator {
    formXInput;
    formYInput;
    formRInput;
    submitButton;

    form_id;

    checkXInput = () => {
        if (this.formXInput.value === "") {
            alert("Выберите значение X.");
            return false;
        }
        if (this.formYInput.value > 3 || this.formYInput.value < -5) {
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
        this.submitButton = document.getElementById(`${form_id}}:submit-button`);
        this.form_id = form_id;

        this.submitButton.addEventListener("click", onSubmit);
        this.formRInput.addEventListener("change", onRInputChange);
    }

    checkAllValid = () => {
        let isNotValid = [
            !this.checkRInput(),
            !this.checkYInput(),
            !this.checkXInput(),
        ].reduce((prev, curr) => prev || curr);

        return !isNotValid;
    }
}

const formValidator = new FormValidator("main-form");

async function loadHitHistory() {
    changeBoardR((+formValidator.formRInput.value).toFixed(0));
    cachedPoints.forEach(element => {
        addPoint(element);
    });
}

loadHitHistory();

const afterAjax = (data) => {

    if (data.status !== 'success') {
        return;
    }

    let x = formValidator.formXInput.value;
    let y = formValidator.formYInput.value;
    let r = formValidator.formRInput.value;

    let hit = document.getElementById("main-form:cached-hit").innerText == "true";
    let serverTime = document.getElementById("main-form:cached-server-time").innerText;

    addPoint({x, y, r, serverTime, hit})
}

const afterAjaxHidden = (data) => {

    if (data.status !== 'success') {
        return;
    }

    let x = +document.getElementById("hidden-form:form-x-input").value;
    let y = +document.getElementById("hidden-form:form-y-input").value;
    let r = +document.getElementById("hidden-form:form-r-input").value;

    let hit = document.getElementById("hidden-form:cached-hit").innerText == "true";
    let serverTime = document.getElementById("hidden-form:cached-server-time").innerText;

    addPoint({x, y, r, serverTime, hit})
}

const onBoardClick = (x,y) => {
    document.getElementById("hidden-form:form-x-input").value = x.toFixed(0);
    document.getElementById("hidden-form:form-y-input").value = y.toFixed(2);
    document.getElementById("hidden-form:form-r-input").value = formValidator.formRInput.value;

    document.getElementById("hidden-form:submit-button").dispatchEvent(new Event('click'));
}

function addPoint({x,y,r,hit,serverTime}) {
    createNewHitPoint(x, y, r, { name: serverTime, color: hit ? "green" : "red", });
}
