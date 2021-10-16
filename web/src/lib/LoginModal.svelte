<script>
    import {fly} from 'svelte/transition';
    import {createEventDispatcher} from 'svelte';
    import CloseButton from "$lib/ui/CloseButton.svelte";
    import {variables} from "$lib/variables";

    const dispatch = createEventDispatcher();

    let userInput, passInput;
    let bg;

    export let prompt = false;
    export let requireLogin = false;

    let errorMsg = false;
    const closeModal = (e) => {
        if (!requireLogin) {
            if (e.target == bg)
                dispatch("closeModal");
        }
    };

    const handleLogin = () => {
        let gateway = variables.gateway;
        errorMsg = false;

        fetch(gateway + "/auth-service/authenticate", {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({
                username: userInput.value,
                password: passInput.value
            })
        }).then(res => {
            if (res.status >= 200 && res.status <= 299)
                return res.json();
            else
                throw Error(res.statusText);
        }).then(data => {
            let token = data.token;
            let details = {
                success: true,
                username: userInput.value,
                token
            }
            localStorage.setItem("jwt", token);
            variables.jwt.set(token);
            dispatch("login", details);
        }).catch(error => {
            errorMsg = error.message;
            // console.log(error);
        });
    };
</script>

<div bind:this={bg} class="container flex-center" on:click={closeModal}>
    <div class="modal"
         on:introend="{() => userInput.focus()}"
         transition:fly="{{ y: 200, duration: 250 }}">
        {#if !requireLogin}
            <CloseButton on:click={() => dispatch("closeModal")}/>
        {/if}
        <div class="modal-body flex-center">
            <h2>Login</h2>
            <div id="form">
                {#if prompt}
                    <div>Please login</div>
                {/if}
                {#if errorMsg}
                    <div class="error">Login failed. ({errorMsg})</div>
                {/if}
                <div>
                    <label for="username">Username: </label>
                    <input bind:this={userInput} id="username" name="username" placeholder="Username" type="text"/>
                </div>
                <div>
                    <label for="password">Password: </label>
                    <input bind:this={passInput} id="password" name="password" placeholder="Password" type="password"/>
                </div>
                <button on:click={handleLogin}>Log In</button>
            </div>
        </div>
    </div>
</div>

<style>
    .error {
        color: red;
    }

    .container {
        position: fixed;
        top: 0;
        left: 0;
        width: 100vw;
        height: 100vh;
        z-index: 100;
        background: rgba(0, 0, 0, 0.4);
        align-items: center;
    }

    .modal {
        background: var(--bg-color);
        max-width: 80%;
        max-height: 80%;
        min-width: 30%;
        min-height: 30%;
        text-align: center;
        padding: 15px;
        box-sizing: border-box;
        border-radius: 15px;
        box-shadow: 5px 5px 5px #666;
        position: relative;
        align-items: stretch;
    }

    h2 {
        margin: 5px;
    }

    .flex-center {
        display: flex;
        justify-content: center;
        flex-direction: column;
    }

    #form {
        position: relative;
        box-sizing: border-box;
    }

    #form div {
        display: flex;
        width: 100%;
        align-items: center;
        justify-content: center;
        padding: 10px;
        box-sizing: border-box;
    }

    label {
        flex-basis: 30%;
        margin-right: 10px;
        min-width: 30%;
    }
</style>