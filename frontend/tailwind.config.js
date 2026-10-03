/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        chopee: {
          orange: '#EE4D2D',
          hover: '#d73211',
          light: '#fef6f5',
          dark: '#c4280b'
        }
      }
    },
  },
  plugins: [],
}

