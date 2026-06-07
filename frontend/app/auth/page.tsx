export default function AuthPage() {
  return (
    <div className="grid two">
      <div className="card">
        <h2>Register</h2>
        <form>
          <input placeholder="Email" />
          <input placeholder="Password" type="password" />
          <button type="button">Register</button>
        </form>
      </div>
      <div className="card">
        <h2>Login</h2>
        <form>
          <input placeholder="Email" />
          <input placeholder="Password" type="password" />
          <button type="button">Login</button>
        </form>
      </div>
    </div>
  );
}
