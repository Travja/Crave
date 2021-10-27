<script context="module">
    import {title, gateway} from "$lib/variables";

    title.set("Item Entry");

    let src = "/find-image.svg";
    let item;

    export async function load({page, fetch}) {
        const url = `${gateway()}/item-service/items/${page.params.item}`;
        console.log(url);
        const res = await fetch(url);

        if (res.ok) {
            let json = await res.json()
            console.log(json);
            item = json;
            return json;
        }

        console.log(res);
    }
</script>

<script>
    let imgUpload;

    const changeImg = e => {
        src = URL.createObjectURL(e.target.files[0]);
    };
</script>

<div class="parent full">
    <div class="hcenter" id="imgBox">
        <img alt="{item.name}" on:click={() => imgUpload.click()} src={item?.image ? item.image : src}/>
        <input accept="image/*" bind:this={imgUpload} id="image" name="image" on:change={changeImg}
               type="file"/>
        <label for="image">Upload an image</label>
    </div>
    <div>
        <h1>{item.name}</h1>
        <hr/>
        <div>{item.description}</div>
        <div class="clear"/>
        <div>Test</div>
    </div>
</div>

<style>
    .parent {
        margin: 10px;
    }

    img {
        width: 30vw;
        height: 30vw;
        max-width: 300px;
        max-height: 300px;
        object-fit: contain;
        box-sizing: border-box;

        border: 2px solid black;
        box-shadow: 5px 5px 5px #333;
        margin-bottom: 10px;
        padding: 10px;
    }

    h1 {
        font-size: 2.3em;
        margin: 0;
    }

    #imgBox {
        float: left;
        margin-bottom: 10px;
        margin-right: 15px;
    }

    img:hover, label:hover {
        cursor: pointer;
    }

    input[type="file"] {
        display: none;
    }
</style>